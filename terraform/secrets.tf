# ADR-020: the demo uses SSM Parameter Store (Standard tier = free) instead of Secrets Manager.
# The app still reads `CLIENT_SECRET` from an env var injected via the ECS task-definition
# `secrets` block (ADR-019's pattern), so it stays secretless + cloud-agnostic.
resource "aws_ssm_parameter" "api_auth_client_secret" {
  name  = "/${local.name}/api-auth-client-secret"
  type  = "SecureString"
  value = var.api_auth_client_secret
  tags  = local.common_tags
}

# AWS-managed KMS key that encrypts SecureString parameters — the task execution role needs
# kms:Decrypt on it to read the value.
data "aws_kms_key" "ssm" {
  key_id = "alias/aws/ssm"
}
