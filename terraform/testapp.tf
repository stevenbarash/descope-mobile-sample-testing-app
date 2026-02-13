terraform {
    required_providers {
        descope = {
            source = "descope/descope"
        }
    }
}

provider "descope" {
    base_url = "https://api.descope.com"
}

// Configurable plan variables

variable "app_domain" {
    type = string
    description = "Your application domain (e.g., myapp.example.com)"
}

variable "google_client_id" {
    type = string
}

variable "google_client_secret" {
    type = string
}

// Descope Project

resource "descope_project" "testapp" {
    name = "Sample App"
    project_settings = {
        app_url = "https://${var.app_domain}"
    }
    authentication = {
        password = {
          disabled = false
        }
        passkeys = {
            top_level_domain = var.app_domain
        }
        oauth = {
            system = {
                google = {
                    client_id = var.google_client_id
                    client_secret = var.google_client_secret
                    allowed_grant_types = ["authorization_code", "implicit"]
                }
            }
        }
    }
    flows = {
        "update-user": {
            data = file("${path.module}/flows/update-user.json"),
        },
        "sign-up-or-in-social": {
            data = file("${path.module}/flows/sign-up-or-in-social.json"),
        },
        "sign-up-or-in-passkeys-or-magic-link": {
            data = file("${path.module}/flows/sign-up-or-in-passkeys-or-magic-link.json"),
        },
    }
}
