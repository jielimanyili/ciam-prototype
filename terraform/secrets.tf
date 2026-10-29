# ADR-019: the demo uses AWS Secrets Manager the same way production does. The app reads
# `CLIENT_SECRET` from an env var injected by the ECS task-definition `secrets` block, so it
# stays secretless + cloud-agnostic.
resource "aws_secretsmanager_secret" "api_auth_client_secret" {
  name = "${local.name}/api-auth-client-secret"
  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "api_auth_client_secret" {
  secret_id     = aws_secretsmanager_secret.api_auth_client_secret.id
  secret_string = var.api_auth_client_secret
}
