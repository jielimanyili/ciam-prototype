resource "aws_ecs_cluster" "main" {
  name = local.name
  tags = local.common_tags
}

resource "aws_security_group" "tasks" {
  name   = "${local.name}-tasks-sg"
  vpc_id = aws_vpc.main.id

  # Only the ALB may reach the tasks.
  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, { Name = "${local.name}-tasks-sg" })
}

resource "aws_cloudwatch_log_group" "jvm" {
  name              = "/ecs/${local.name}/api-identity-jvm"
  retention_in_days = 7
  tags              = local.common_tags
}

resource "aws_cloudwatch_log_group" "python" {
  name              = "/ecs/${local.name}/api-identity-python"
  retention_in_days = 7
  tags              = local.common_tags
}

resource "aws_cloudwatch_log_group" "auth" {
  name              = "/ecs/${local.name}/api-auth"
  retention_in_days = 7
  tags              = local.common_tags
}
