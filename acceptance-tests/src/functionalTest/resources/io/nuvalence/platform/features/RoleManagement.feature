Feature: Role Management

  Scenario: Application Role Configuration
    Given a user with the following roles
      | role                  |
      | um:application-client |
    And an application role manifest named "test-application-0" with roles
      | applicationRole | name       | description     | group |
      | ta:basic        | Basic User | Basic user role | test  |
      | ta:admin        | Admin User | Admin user role | test  |
    When the application role manifest is PUT to the API
    Then it should return 204
    When a user with the following roles
      | role                  |
      | um:admin |
    And a GET request is made for application roles
    Then it should return 200
    And the response should contain application roles
      | applicationRole | name       | description     | group |
      | ta:basic        | Basic User | Basic user role | test  |
      | ta:admin        | Admin User | Admin user role | test  |



