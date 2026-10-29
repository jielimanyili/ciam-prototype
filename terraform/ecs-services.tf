resource "aws_ecs_service" "jvm" {
  name            = "api-identity-jvm"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.jvm.arn
  desired_count   = 1
  launch_type     = "EC2"

  load_balancer {
    target_group_arn = aws_lb_target_group.jvm.arn
    container_name   = "api-identity-jvm"
    container_port   = local.jvm_port
  }
  tags = local.common_tags
}

resource "aws_ecs_service" "python" {
  name            = "api-identity-python"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.python.arn
  desired_count   = 1
  launch_type     = "EC2"

  load_balancer {
    target_group_arn = aws_lb_target_group.python.arn
    container_name   = "api-identity-python"
    container_port   = local.python_port
  }
  tags = local.common_tags
}

resource "aws_ecs_service" "auth" {
  name            = "api-auth"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.auth.arn
  desired_count   = 1
  launch_type     = "EC2"

  load_balancer {
    target_group_arn = aws_lb_target_group.auth.arn
    container_name   = "api-auth"
    container_port   = local.auth_port
  }
  tags = local.common_tags
}
