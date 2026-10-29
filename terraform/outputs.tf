output "app_url" {
  description = "Public SPA URL (S3 + CloudFront)."
  value       = "https://app.${var.domain_name}"
}

output "api_jvm_url" {
  description = "JVM resource server (behind the ALB)."
  value       = "https://api.${var.domain_name}"
}

output "api_python_url" {
  description = "Python resource server (behind the ALB)."
  value       = "https://api-py.${var.domain_name}"
}

output "auth_url" {
  description = "BFF (api-auth) behind the ALB."
  value       = "https://auth.${var.domain_name}"
}

output "spa_bucket" {
  description = "S3 bucket name for the SPA (CI sync target)."
  value       = aws_s3_bucket.spa.id
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution id (CI invalidation target)."
  value       = aws_cloudfront_distribution.main.id
}

output "alb_dns_name" {
  description = "Raw ALB DNS name (pre-DNS)."
  value       = aws_lb.main.dns_name
}

output "ecr_repositories" {
  description = "ECR repository URLs (push targets for CI)."
  value = {
    jvm    = aws_ecr_repository.jvm.repository_url
    python = aws_ecr_repository.python.repository_url
    auth   = aws_ecr_repository.auth.repository_url
  }
}

output "client_secret_arn" {
  description = "Secrets Manager ARN for the api-auth client secret (ADR-019)."
  value       = aws_secretsmanager_secret.api_auth_client_secret.arn
}
