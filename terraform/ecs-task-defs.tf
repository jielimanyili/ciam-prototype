resource "aws_ecs_task_definition" "jvm" {
  family                   = "${local.name}-api-identity-jvm"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-identity-jvm"
      image        = "${aws_ecr_repository.jvm.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.jvm_port, protocol = "tcp" }]
      environment = [
        { name = "OIDC_ISSUER", value = var.oidc_issuer },
        { name = "OIDC_AUDIENCE", value = var.oidc_audience_jvm },
        { name = "PORT", value = tostring(local.jvm_port) },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.jvm.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "jvm"
        }
      }
    }
  ])
}

resource "aws_ecs_task_definition" "python" {
  family                   = "${local.name}-api-identity-python"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-identity-python"
      image        = "${aws_ecr_repository.python.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.python_port, protocol = "tcp" }]
      environment = [
        { name = "OIDC_ISSUER", value = var.oidc_issuer },
        { name = "OIDC_AUDIENCE", value = var.oidc_audience_python },
        { name = "PORT", value = tostring(local.python_port) },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.python.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "python"
        }
      }
    }
  ])
}

resource "aws_ecs_task_definition" "auth" {
  family                   = "${local.name}-api-auth"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-auth"
      image        = "${aws_ecr_repository.auth.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.auth_port, protocol = "tcp" }]
      environment = [
        { name = "OIDC_ISSUER", value = var.oidc_issuer },
        # BFF proxies downstream to the JVM resource server via the same ALB.
        { name = "RESOURCE_SERVER_URL", value = "https://api.${var.domain_name}" },
        { name = "PORT", value = tostring(local.auth_port) },
      ]
      # ADR-019: inject the client secret from Secrets Manager (never in the task definition).
      secrets = [
        { name = "CLIENT_SECRET", valueFrom = aws_secretsmanager_secret.api_auth_client_secret.arn }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.auth.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "auth"
        }
      }
    }
  ])
}
