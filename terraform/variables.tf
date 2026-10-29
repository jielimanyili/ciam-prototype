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

variable "oidc_issuer" {
  description = "OIDC issuer (Okta dev tenant)."
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
  description = "api-auth OIDC client secret (Okta). Stored in Secrets Manager — never committed (ADR-019)."
  type        = string
  sensitive   = true
}

variable "domain_name" {
  description = "Public domain for the demo (e.g. demo.example.com). Host-routes the APIs and fronts the SPA over HTTPS."
  type        = string
}

variable "route53_zone_id" {
  description = "Route53 hosted-zone id for ACM DNS validation + ALIAS records."
  type        = string
}

variable "saml_enabled" {
  description = "Enable SAML 2.0 on the api-auth BFF (Epic 2). Set false for an OIDC-only Okta demo."
  type        = bool
  default     = false
}

variable "saml_idp_metadata_uri" {
  description = "SAML IdP descriptor URL for api-auth when saml_enabled=true (e.g. an Okta SAML app's metadata URL)."
  type        = string
  default     = ""
}
