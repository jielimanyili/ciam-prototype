resource "aws_ecr_repository" "jvm" {
  name                 = "${local.name}/api-identity-jvm"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  tags                 = local.common_tags
}

resource "aws_ecr_repository" "python" {
  name                 = "${local.name}/api-identity-python"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  tags                 = local.common_tags
}

resource "aws_ecr_repository" "auth" {
  name                 = "${local.name}/api-auth"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  tags                 = local.common_tags
}
