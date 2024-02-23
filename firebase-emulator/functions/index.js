const functions = require("firebase-functions");
const {SecretManagerServiceClient} = require('@google-cloud/secret-manager');
const axios = require('axios')
const jwt = require('jsonwebtoken');
const cache = require('memory-cache');
const config = require('./config');

exports.beforeCreate = functions.auth.user().beforeCreate(async (user) => {
    console.log(`User ${user.uid} with email ${user.email} signed up`);
    console.log(`User: ${JSON.stringify(user)}`);
    const { providerData } = user;
    const providerId = providerData[0].providerId;

    try {
        console.log(`Creating user with providerId ${providerId} and tenantId ${user.tenantId}`);

        if (isAgencyUser(user.tenantId)) {
            console.log(`Detected agency user with tenantId ${user.tenantId}`);
            const apiUser = await createUserOfType('agency', user);

            const customClaims = {
                user_type: "agency",
                application_user_id: apiUser.id,
                roles: getAgencyRoles()
            };
            console.log(`Setting custom claims for user ${user.email} with claims ${JSON.stringify(customClaims)}`);
            return {
                customClaims: customClaims
            };
        } else {
            console.log(`Detected public user with tenantId ${user.tenantId}`);
            const apiUser = await createUserOfType('public', user);

            const customClaims = {
                user_type: "public",
                application_user_id: apiUser.id,
                roles: getPublicRoles()
            }
            console.log(`Setting custom claims for user ${user.email} with claims ${JSON.stringify(customClaims)}`);
            return {
                customClaims: customClaims
            };
        }
    } catch (e) {
        console.error('User creation failed with error', {error: e});
        if (e instanceof functions.https.HttpsError) {
            throw e;
        }

        throw new functions.https.HttpsError('internal', 'An unexpected error has occurred and has been logged.');
    }
});

const isAgencyUser = (tenantId) => {
    return tenantId === config.agencyTenantId;
}

const getAgencyRoles = () => {
    return [
        "as:event-reader",
        "dm:document-reviewer",
        "dm:document-uploader",
        "um:reader",
        "um:admin",
        "wm:agency-profile-admin",
        "wm:transaction-admin",
        "wm:transaction-config-admin",
        "ns:notification-admin"
    ];
}

const getPublicRoles = () => {
    return [
        "dm:document-uploader",
        "um:basic",
        "wm:employer-user",
        "wm:individual-user",
        "wm:transaction-submitter",
        "wm:public-profile-user"
    ];
}

const createUserOfType = async (userType, user) => {
    return await createUser({
        displayName: user.displayName || user.email,
        email: user.email,
        externalId: user.uid,
        identityProvider: config.identityProvider,
        userType: userType,
    });
}

const createUser = async (data) => {
    console.log(`Creating user with ${JSON.stringify(data)}`);
    const jwtSigningKey = config.jwtSigningPrivateKey ?? await accessSecret(config.jwtSigningPrivateKeySecret);
    const options = {
        headers:{
            'Authorization': `Bearer ${getAuthToken(jwtSigningKey)}`,
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        }
    };

    try {
        const response = await axios.post(`${config.userManagementBaseUrl}/api/v1/users`, data, options);
        console.log('User successfully created.', {userData: JSON.stringify(response.data)});
        return response.data;
    } catch (e) {
        console.error('User creation failed with error', {error: e});
        const status = e.response.status;

        if (status === 409) {
            const message = e.response.data.message;
            throw new functions.https.HttpsError('already-exists', message);
        }

        throw e;
    }
};

async function accessSecret(fullName) {
    try {
        const client = new SecretManagerServiceClient();
        const [response] = await client.accessSecretVersion({name: fullName});
        return response.payload.data.toString();
    }
    catch (e) {
        console.error(`Error accessing secret ${fullName}`, {error: e});
    }
}

const getAuthToken = (jwtSigningKey) => {
    let token = cache.get('token');
    if (!token) {
        token = generateToken(jwtSigningKey);
        cache.put('token', token, 180_000); // cache for 3 minutes
    }

    return token;
}

const generateToken = (jwtSigningKey) => {
    const payload = {
        roles: [
            'um:identity-client'
        ]
    };
    const options = {
        algorithm: 'RS256',
        expiresIn: '5m',
        issuer: config.jwtIssuer,
        subject: 'dsgov-identity-functions'
    };
    return jwt.sign(payload, jwtSigningKey, options);
}
