variable "region" {
  description = "AWS region. CloudFront requires its ACM certificate in us-east-1, so keep the default."
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Project/stack name used as a resource-name prefix."
  type        = string
  default     = "ciam"
}

variable "environment" {
  description = "Deployment environment (suffix on resource names)."
  type        = string
  default     = "demo"
}

variable "image_tag" {
  description = "Docker image tag to deploy (git SHA from CI; 'latest' for manual runs)."
  type        = string
  default     = "latest"
}

variable "instance_type" {
  description = "EC2 instance type for the free-tier ECS host (t2.micro/t3.micro = 750 free h/month)."
  type        = string
  default     = "t3.micro"
}

variable "oidc_issuer" {
  description = "OIDC issuer (Auth0 tenant). Include the trailing slash — Auth0's iss claim carries it (e.g. https://dev-x.us.auth0.com/)."
  type        = string
  default     = "http://localhost:8080/realms/ciam-demo"
}

variable "oidc_audience_jvm" {
  description = "Audience the JVM resource server requires (ADR-013)."
  type        = string
  default     = "api-identity-jvm"
}

variable "oidc_audience_python" {
  description = "Audience the Python resource server requires (ADR-013)."
  type        = string
  default     = "api-identity-python"
}

variable "api_auth_client_secret" {
  description = "api-auth OIDC client secret (Auth0 Regular Web App). Stored in SSM Parameter Store — never committed (ADR-020)."
  type        = string
  sensitive   = true
}

variable "saml_enabled" {
  description = "Enable SAML 2.0 on the api-auth BFF (Epic 2). Set false for an OIDC-only Auth0 demo."
  type        = bool
  default     = false
}

variable "saml_idp_metadata_uri" {
  description = "SAML IdP descriptor URL for api-auth when saml_enabled=true (e.g. a SAML IdP's metadata URL)."
  type        = string
  default     = ""
}
