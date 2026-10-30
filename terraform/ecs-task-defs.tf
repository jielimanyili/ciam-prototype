resource "aws_ecs_task_definition" "jvm" {
  family             = "${local.name}-api-identity-jvm"
  network_mode       = "bridge"
  cpu                = "256"
  memory             = "300"
  execution_role_arn = aws_iam_role.task_execution.arn
  task_role_arn      = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-identity-jvm"
      image        = "${aws_ecr_repository.jvm.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.jvm_port, hostPort = local.jvm_port, protocol = "tcp" }]
      environment = [
        { name = "OIDC_ISSUER", value = var.oidc_issuer },
        { name = "OIDC_AUDIENCE", value = var.oidc_audience_jvm },
        { name = "PORT", value = tostring(local.jvm_port) },
        { name = "CONTEXT_PATH", value = "/jvm" },
        # Tuned for the free-tier 1 GB instance (see ec2.tf / ADR-016).
        { name = "JAVA_TOOL_OPTIONS", value = "-Xmx192m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC" },
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
  family             = "${local.name}-api-identity-python"
  network_mode       = "bridge"
  cpu                = "128"
  memory             = "128"
  execution_role_arn = aws_iam_role.task_execution.arn
  task_role_arn      = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-identity-python"
      image        = "${aws_ecr_repository.python.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.python_port, hostPort = local.python_port, protocol = "tcp" }]
      environment = [
        { name = "OIDC_ISSUER", value = var.oidc_issuer },
        { name = "OIDC_AUDIENCE", value = var.oidc_audience_python },
        { name = "PORT", value = tostring(local.python_port) },
        { name = "CONTEXT_PATH", value = "/python" },
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
  family             = "${local.name}-api-auth"
  network_mode       = "bridge"
  cpu                = "256"
  memory             = "300"
  execution_role_arn = aws_iam_role.task_execution.arn
  task_role_arn      = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name         = "api-auth"
      image        = "${aws_ecr_repository.auth.repository_url}:${var.image_tag}"
      portMappings = [{ containerPort = local.auth_port, hostPort = local.auth_port, protocol = "tcp" }]
      environment = concat(
        [
          # BFF: issuer WITHOUT a trailing slash — Spring appends /.well-known/... and a trailing
          # slash would 404 Auth0's discovery doc. Resource servers keep the full trailing-slash issuer.
          { name = "OIDC_ISSUER", value = trimsuffix(var.oidc_issuer, "/") },
          { name = "OIDC_AUDIENCE", value = var.oidc_audience_jvm },
          # BFF proxies downstream to the JVM resource server via the same ALB (context-path /jvm).
          { name = "RESOURCE_SERVER_URL", value = "http://${aws_lb.main.dns_name}/jvm/" },
          { name = "PORT", value = tostring(local.auth_port) },
          { name = "CONTEXT_PATH", value = "/auth" },
          { name = "SAML_ENABLED", value = tostring(var.saml_enabled) },
          # Tuned for the free-tier 1 GB instance (see ec2.tf / ADR-016).
          { name = "JAVA_TOOL_OPTIONS", value = "-Xmx192m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC" },
        ],
        var.saml_idp_metadata_uri != "" ? [{ name = "SAML_IDP_METADATA_URI", value = var.saml_idp_metadata_uri }] : []
      )
      # ADR-020: inject the client secret from SSM Parameter Store (never in the task definition).
      secrets = [
        { name = "CLIENT_SECRET", valueFrom = aws_ssm_parameter.api_auth_client_secret.arn }
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
