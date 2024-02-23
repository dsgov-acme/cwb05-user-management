const { beforeCreate } = require('../index');
const axios = require('axios');
const jwt = require('jsonwebtoken');
const cache = require('memory-cache');
const functions = require('firebase-functions');
const config = require('../config');

jest.mock('axios');
jest.mock('jsonwebtoken');
jest.mock('memory-cache');
jest.mock('firebase-functions', () => ({
    auth: {
        user: () => ({
            beforeCreate: (handler) => handler,
        }),
    },
    https: {
        HttpsError: jest.fn(),
    },
}));

jest.mock('../config', () => ({
    agencyTenantId: 'agencyTenantId',
    jwtSigningPrivateKeySecret: 'projects/my-project/secrets/my-secret/versions/latest',
    jwtSigningPrivateKey: 'fakePrivateKey',
    jwtIssuer: 'my-issuer',
    userManagementBaseUrl: 'http://localhost:8080',
    gcpProject: 'my-firebase-project',
}));

functions.https.HttpsError.mockImplementation((code, message) => {
    const error = new Error(message);
    error.code = code;
    return error;
});

describe('beforeCreate', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should create a public user and set custom claims', async () => {
        const user = {
            uid: '1',
            email: 'test@test.com',
            displayName: 'Test User',
            providerData: [{ providerId: 'password' }],
            tenantId: 'publicTenantId',
        };

        axios.post.mockResolvedValue({ data: { id: 'publicUserId' } });
        jwt.sign.mockReturnValue('fakeJwtToken');
        cache.get.mockReturnValue(null);
        cache.put.mockImplementation(() => {});

        const result = await beforeCreate(user);

        expect(axios.post).toHaveBeenCalledWith(
            `${config.userManagementBaseUrl}/api/v1/users`,
            expect.any(Object),
            expect.any(Object)
        );

        expect(result.customClaims).toEqual(getPublicCustomClaims());
    });

    it('should create an agency user and set custom claims', async () => {
        const user = {
            uid: '2',
            email: 'agency@test.com',
            displayName: 'Agency User',
            providerData: [{ providerId: 'saml.agency' }],
            tenantId: config.agencyTenantId,
        };

        axios.post.mockResolvedValue({ data: { id: 'agencyUserId' } });
        jwt.sign.mockReturnValue('fakeJwtToken');
        cache.get.mockReturnValue(null);
        cache.put.mockImplementation(() => {});

        const result = await beforeCreate(user);

        expect(axios.post).toHaveBeenCalledWith(
            `${config.userManagementBaseUrl}/api/v1/users`,
            expect.any(Object),
            expect.any(Object)
        );

        expect(result.customClaims).toEqual(getAgencyCustomClaims());
    });

    it('should handle user creation failure due to axios error', async () => {
        const user = {
            uid: '3',
            email: 'fail@test.com',
            displayName: 'Fail User',
            providerData: [{ providerId: 'password' }],
            tenantId: 'publicTenantId',
        };

        const errorResponse = {
            response: {
                status: 500,
                data: { message: 'Internal Server Error' },
            },
        };

        axios.post.mockRejectedValue(errorResponse);

        await expect(beforeCreate(user)).rejects.toThrow('An unexpected error has occurred and has been logged.');
        expect(functions.https.HttpsError).toHaveBeenCalledWith('internal', 'An unexpected error has occurred and has been logged.');
    });

    it('should handle user creation failure due to conflict', async () => {
        const user = {
            uid: '4',
            email: 'conflict@test.com',
            displayName: 'Conflict User',
            providerData: [{ providerId: 'password' }],
            tenantId: 'publicTenantId',
        };

        const errorResponse = {
            response: {
                status: 409,
                data: { message: 'User already exists' },
            },
        };

        axios.post.mockRejectedValue(errorResponse);

        await expect(beforeCreate(user)).rejects.toThrow('An unexpected error has occurred and has been logged.');
        expect(functions.https.HttpsError).toHaveBeenCalledWith('already-exists', 'User already exists');
    });

});

const getPublicCustomClaims = () => {
    return {
        user_type: "public",
        application_user_id: "publicUserId",
        roles: [
            "dm:document-uploader",
            "um:basic",
            "wm:employer-user",
            "wm:individual-user",
            "wm:transaction-submitter",
            "wm:public-profile-user"
        ]
    };
};

const getAgencyCustomClaims = () => {
    return {
        user_type: "agency",
        application_user_id: "agencyUserId",
        roles: [
            "as:event-reader",
            "dm:document-reviewer",
            "dm:document-uploader",
            "um:reader",
            "um:admin",
            "wm:agency-profile-admin",
            "wm:transaction-admin",
            "wm:transaction-config-admin",
            "ns:notification-admin"
        ]
    };
};
