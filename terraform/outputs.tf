output "app_url" {
  description = "Public SPA URL (S3 + CloudFront, free *.cloudfront.net domain)."
  value       = "https://${aws_cloudfront_distribution.main.domain_name}"
}

output "api_jvm_url" {
  description = "JVM resource server (CloudFront path)."
  value       = "https://${aws_cloudfront_distribution.main.domain_name}/jvm"
}

output "api_python_url" {
  description = "Python resource server (CloudFront path)."
  value       = "https://${aws_cloudfront_distribution.main.domain_name}/python"
}

output "auth_url" {
  description = "BFF (api-auth) behind CloudFront."
  value       = "https://${aws_cloudfront_distribution.main.domain_name}/auth"
}

output "spa_bucket" {
  description = "S3 bucket name for the SPA (CI sync target)."
  value       = aws_s3_bucket.spa.id
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution id (CI invalidation target)."
  value       = aws_cloudfront_distribution.main.id
}

output "cloudfront_domain_name" {
  description = "The free *.cloudfront.net domain (for Okta redirect URIs)."
  value       = aws_cloudfront_distribution.main.domain_name
}

output "alb_dns_name" {
  description = "Raw ALB DNS name (CloudFront origin)."
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

output "client_secret_param" {
  description = "SSM Parameter (SecureString) holding the api-auth client secret (ADR-020)."
  value       = aws_ssm_parameter.api_auth_client_secret.arn
}
