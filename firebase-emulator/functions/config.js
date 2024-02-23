const GCP_PROJECT = process.env.GCP_PROJECT || '';

const config = {
    gcpProject: GCP_PROJECT,
    agencyTenantId: process.env.AGENCY_TENANT_ID || '',
    publicTenantId: process.env.PUBLIC_TENANT_ID || '',
    jwtIssuer: process.env.JWT_ISSUER || 'dsgov',
    jwtSigningPrivateKey: process.env.JWT_SIGNING_PRIVATE_KEY,
    jwtSigningPrivateKeySecret: process.env.JWT_SIGNING_PRIVATE_KEY_SECRET || '',
    userManagementBaseUrl: process.env.USER_MANAGEMENT_BASE_URL || '',
    identityProvider: `https://securetoken.google.com/${GCP_PROJECT}`
}
module.exports = config;
