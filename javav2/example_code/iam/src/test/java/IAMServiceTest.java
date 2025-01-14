import com.example.iam.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKey;
import software.amazon.awssdk.services.iam.model.User;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IAMServiceTest {

    private static IamClient iam;
    private static String userName="";
    private static String policyName="";
    private static String roleName="";
    private static String policyARN=""; // Set in test 3
    private static String keyId ="" ; // set in test 4
    private static String accountAlias="";

    // Create data members to test the IAMScenario
    private static String usernameSc = "";
    private static String policyNameSc = "";
    private static String roleNameSc = "";
    private static String roleSessionName = "";
    private static String fileLocationSc = "";
    private static String bucketNameSc = "";

    @BeforeAll
    public static void setUp() throws IOException {

        Region region = Region.AWS_GLOBAL;
        iam = IamClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        try (InputStream input = IAMServiceTest.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();
            prop.load(input);
            // Populate the data members required for all tests
            userName = prop.getProperty("userName");
            policyName= prop.getProperty("policyName");
            policyARN= prop.getProperty("policyARN");
            roleName=prop.getProperty("roleName");
            accountAlias=prop.getProperty("accountAlias");
            usernameSc=prop.getProperty("usernameSc");
            policyNameSc=prop.getProperty("policyNameSc");
            roleNameSc=prop.getProperty("roleNameSc");
            roleSessionName=prop.getProperty("roleSessionName");
            fileLocationSc=prop.getProperty("fileLocationSc");
            bucketNameSc=prop.getProperty("bucketNameSc");

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @Order(1)
    public void whenInitializingAWSIAMService_thenNotNull() {
        assertNotNull(iam);
        System.out.printf("\n Test 1 passed");
    }

    @Test
    @Order(2)
    public void CreatUser() {

        String result = CreateUser.createIAMUser(iam, userName);
        assertTrue(!result.isEmpty());
        System.out.println("\n Test 2 passed");
    }

    @Test
    @Order(3)
    public void CreatePolicy() {

         policyARN = CreatePolicy.createIAMPolicy(iam, policyName);
         assertTrue(!policyARN.isEmpty());
         System.out.println("\n Test 3 passed");
    }

    @Test
    @Order(4)
    public void CreateAccessKey() {

        keyId = CreateAccessKey.createIAMAccessKey(iam,userName);
        assertTrue(!keyId.isEmpty());
        System.out.println("\n Test 4 passed");
    }

    @Test
    @Order(5)
    public void AttachRolePolicy() {

       AttachRolePolicy.attachIAMRolePolicy(iam, roleName, policyARN );
       System.out.println("\n Test 5 passed");
    }

    @Test
    @Order(6)
    public void DetachRolePolicy() {

        DetachRolePolicy.detachPolicy(iam, roleName, policyARN);
        System.out.println("\n Test 6 passed");
    }

    @Test
    @Order(7)
    public void GetPolicy() {

        GetPolicy.getIAMPolicy(iam, policyARN);
        System.out.println("\n Test 7 passed");
    }

    @Test
    @Order(8)
    public void ListAccessKeys() {

        ListAccessKeys.listKeys(iam,userName);
        System.out.println("\n Test 8 passed");
    }

    @Test
    @Order(9)
    public void ListUsers() {

       ListUsers.listAllUsers(iam);
       System.out.println("\n Test 9 passed");
   }

    @Test
    @Order(10)
   public void CreateAccountAlias() {

       CreateAccountAlias.createIAMAccountAlias(iam, accountAlias);
       System.out.println("\n Test 10 passed");
    }

    @Test
    @Order(11)
    public void DeleteAccountAlias() {

        DeleteAccountAlias.deleteIAMAccountAlias(iam, accountAlias);
        System.out.println("\n Test 11 passed");
    }

    @Test
    @Order(12)
    public void DeletePolicy() {

       DeletePolicy.deleteIAMPolicy(iam, policyARN);
       System.out.println("\n Test 12 passed");
    }

    @Test
    @Order(13)
   public void DeleteAccessKey() {

       DeleteAccessKey.deleteKey(iam, userName, keyId);
       System.out.println("\n Test 13 passed");
   }

    @Test
    @Order(14)
    public void DeleteUser() {

        DeleteUser.deleteIAMUser(iam,userName);
        System.out.println("\n Test 14 passed");
    }

    @Test
    @Order(14)
    public void TestIAMScenario() throws Exception {
        String DASHES = new String(new char[80]).replace("\0", "-");
        System.out.println(DASHES);
        System.out.println(" 1. Create the IAM user.");
        User createUser = IAMScenario.createIAMUser(iam, usernameSc);

        System.out.println(DASHES);
        String userArn = createUser.arn();
        AccessKey myKey = IAMScenario.createIAMAccessKey(iam, usernameSc);
        String accessKey = myKey.accessKeyId();
        String secretKey = myKey.secretAccessKey();
        String assumeRolePolicyDocument = "{" +
            "\"Version\": \"2012-10-17\"," +
            "\"Statement\": [{" +
            "\"Effect\": \"Allow\"," +
            "\"Principal\": {" +
            "	\"AWS\": \"" + userArn + "\"" +
            "}," +
            "\"Action\": \"sts:AssumeRole\"" +
            "}]" +
            "}";

        System.out.println(assumeRolePolicyDocument);
        System.out.println(usernameSc + " was successfully created.");
        System.out.println(DASHES);
        System.out.println("2. Creates a policy.");
        String polArn = IAMScenario.createIAMPolicy(iam, policyNameSc);
        System.out.println("The policy " + polArn + " was successfully created.");
        System.out.println(DASHES);

        System.out.println(DASHES);
        System.out.println("3. Creates a role.");
        TimeUnit.SECONDS.sleep(30);
        String roleArn = IAMScenario.createIAMRole(iam, roleNameSc, assumeRolePolicyDocument);
        System.out.println(roleArn + " was successfully created.");
        System.out.println(DASHES);

        System.out.println(DASHES);
        System.out.println("4. Grants the user permissions.");
        IAMScenario.attachIAMRolePolicy(iam, roleNameSc, polArn);
        System.out.println(DASHES);

        System.out.println(DASHES);
        System.out.println("*** Wait for 30 secs so the resource is available");
        TimeUnit.SECONDS.sleep(30);
        System.out.println("5. Gets temporary credentials by assuming the role.");
        System.out.println("Perform an Amazon S3 Service operation using the temporary credentials.");
        IAMScenario.assumeGivenRole(roleArn, roleSessionName, bucketNameSc, accessKey, secretKey);
        System.out.println(DASHES);

        System.out.println(DASHES);
        System.out.println("6 Getting ready to delete the AWS resources");
        IAMScenario.deleteKey(iam, usernameSc, accessKey );
        IAMScenario.deleteRole(iam, roleNameSc, polArn);
        IAMScenario.deleteIAMUser(iam, usernameSc);
        System.out.println(DASHES);
    }
}
