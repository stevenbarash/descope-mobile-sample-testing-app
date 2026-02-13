# Terraform Configuration

This Terraform configuration provisions a Descope project for the sample application.

## Requirements

- [Terraform](https://www.terraform.io/downloads) >= 1.0
- [Descope Terraform Provider](https://registry.terraform.io/providers/descope/descope/latest)
- Descope Management Key (from [Descope Console](https://app.descope.com) → Company → Management Keys)

## Required Variables

| Variable | Description |
|----------|-------------|
| `app_domain` | Your application domain (e.g., `myapp.example.com`) |
| `google_client_id` | Google OAuth client ID |
| `google_client_secret` | Google OAuth client secret |

## Usage

1. Export the Descope management key:
   ```bash
   export DESCOPE_MANAGEMENT_KEY="<your-management-key>"
   ```

2. Create a `testapp.tfvars` file with the required variables:
   ```hcl
   app_domain           = "myapp.example.com"
   google_client_id     = "<google-client-id>"
   google_client_secret = "<google-client-secret>"
   ```

3. Initialize Terraform:
   ```bash
   terraform init
   ```

4. Apply the configuration:
   ```bash
   terraform apply -var-file=testapp.tfvars
   ```

## Flows

This configuration includes the following Descope flows:
- `sign-up-or-in-passkeys-or-magic-link`
- `sign-up-or-in-social`
- `update-user`
