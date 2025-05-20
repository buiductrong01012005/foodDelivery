package com.example.fooddelivery.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test Suite để chạy tất cả các test cho LoginController
 */
@Suite
@SelectClasses({
    LoginTest.class,
    LoginSecurityTest.class,
    UserRoleTest.class
})
public class LoginTestSuite {
    // Không cần nội dung cho Test Suite
}
