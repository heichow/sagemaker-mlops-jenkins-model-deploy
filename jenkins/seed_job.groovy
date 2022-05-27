import javaposse.jobdsl.dsl.DslException
import jenkins.model.Jenkins
import hudson.model.AbstractProject

// Check if AWS credential parameter is passed or not
/*
def awsCredentialId = getBinding().getVariables()['AWS_CREDENTIAL']
if (awsCredentialId == null) {
  throw new DslException('Please pass AWS credential parameter ' + 'AWS_CREDENTIAL' )
}
*/

// Need modification
def awsRegion = "us-east-2"
//def artifactBucket = "sagemaker-us-east-1-468208999430"
def sagemakerProjectName = "mlops-jenkins-yolov4"
//def sagemakerProjectId = "p-hdzaivmsdryg"
def sourceModelPackageGroupName = "mlops-jenkins-yolov4"
def modelExecutionRole = "arn:aws:iam::221110525845:role/sagemaker-model-registry-SageMakerExecutionRole-1FWZ6RQ3B5JOQ"
def stagingStackName = "sagemaker-mlops-jenkins-yolov4-deploy-staging"
def prodStackName = "sagemaker-mlops-jenkins-yolov4-deploy-prod"
//def pipelineName = "sagemaker-" + sagemakerProjectName + "-" + sagemakerProjectId + "-modeldeploy"
def pipelineName = "sagemaker-" + sagemakerProjectName + "-modeldeploy" //DO NOT CHANGE! Hard code in Lambda webhook function


// Get git details used in JOB DSL so that can be used for pipeline SCM also
def jobName = getBinding().getVariables()['JOB_NAME']
def gitUrl = getBinding().getVariables()['GIT_URL']
def gitBranch = getBinding().getVariables()['GIT_BRANCH']
def jenkins = Jenkins.getInstance()
def job = (AbstractProject)jenkins.getItem(jobName)
def remoteSCM = job.getScm()
def credentialsId = remoteSCM.getUserRemoteConfigs()[0].getCredentialsId()

pipelineJob(pipelineName) {
  description("Sagemaker Model Deploy Pipeline")
  keepDependencies(false)
  authenticationToken('token')
  //concurrentBuild(false)
  parameters {
    stringParam("AWS_REGION", awsRegion, "AWS region to use for creating entity")
    //stringParam("ARTIFACT_BUCKET", artifactBucket, "S3 bucket to store training artifact")
    stringParam("SAGEMAKER_PROJECT_NAME", sagemakerProjectName, "Sagemaker Project Name")
    // stringParam("SAGEMAKER_PROJECT_ID", sagemakerProjectId, "Sagemaker Project Id")
    stringParam("SOURCE_MODEL_PACKAGE_GROUP_NAME", sourceModelPackageGroupName, "Model Package Group Name")
    stringParam("MODEL_EXECUTION_ROLE_ARN", modelExecutionRole, "Role to be used by Model execution.")
    stringParam("STACK_NAME_DEPLOY_STAGING", stagingStackName, "CloudFormation Stack Name of staging environment for update.")
    stringParam("STACK_NAME_DEPLOY_PROD", prodStackName, "CloudFormation Stack Name of production environment for update.")
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            url(gitUrl)
            credentials(credentialsId)
          }
          branch(gitBranch)
        }
      }
      scriptPath("jenkins/Jenkinsfile")
    }
  }
  disabled(false)
  triggers {
    scm("* * * * *")
  }
}