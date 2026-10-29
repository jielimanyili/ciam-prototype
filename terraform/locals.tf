locals {
  name = "${var.project}-${var.environment}"

  jvm_port    = 4101
  python_port = 4102
  auth_port   = 4103

  common_tags = {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}
