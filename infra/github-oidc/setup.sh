#!/usr/bin/env bash
set -euo pipefail

# One-time setup: GitHub Actions OIDC -> AWS (ADR-018). Run after `aws configure`.
#
# Usage:
#   export ACCOUNT_ID=123456789012                    # required (12-digit AWS account id)
#   export ROLE_NAME=github-actions-ciam-deploy       # optional
#   export REPO=jielimanyili/ciam-prototype           # optional
#   ./setup.sh
#
# Requires: AWS CLI v2 + credentials with iam:CreateOpenIDConnectProvider + iam:CreateRole.

ACCOUNT_ID="${ACCOUNT_ID:?set ACCOUNT_ID to your 12-digit AWS account id}"
ROLE_NAME="${ROLE_NAME:-github-actions-ciam-deploy}"
REPO="${REPO:-jielimanyili/ciam-prototype}"
# GitHub Actions' stable OIDC thumbprint.
GITHUB_THUMBPRINT="6938fd4d98bab03faadb97b34396831e3780aea1"

# 1. Register GitHub's OIDC provider (idempotent — skip if it already exists).
if ! aws iam get-open-id-connect-provider \
  --open-id-connect-provider-arn "arn:aws:iam::${ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com" >/dev/null 2>&1; then
  aws iam create-open-id-connect-provider \
    --url https://token.actions.githubusercontent.com \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list "${GITHUB_THUMBPRINT}"
fi

# 2. Render the trust policy with the real account id + repo.
TRUST_POLICY="$(mktemp)"
cat > "${TRUST_POLICY}" <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::${ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:${REPO}:*"
        }
      }
    }
  ]
}
EOF

# 3. Create (or update) the role.
if aws iam get-role --role-name "${ROLE_NAME}" >/dev/null 2>&1; then
  aws iam update-assume-role-policy --role-name "${ROLE_NAME}" --policy-document "file://${TRUST_POLICY}"
else
  aws iam create-role --role-name "${ROLE_NAME}" --assume-role-policy-document "file://${TRUST_POLICY}"
fi

# 4. Attach the scoped deploy policy (service-scoped; * on resources — demo-level least privilege).
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
aws iam put-role-policy \
  --role-name "${ROLE_NAME}" \
  --policy-name ciam-deploy \
  --policy-document "file://${SCRIPT_DIR}/deploy-policy.json"

rm -f "${TRUST_POLICY}"
echo
echo "Done."
echo "Set GitHub secret  AWS_ROLE_ARN = arn:aws:iam::${ACCOUNT_ID}:role/${ROLE_NAME}"
