packer {
  required_plugins {
    amazon = {
      version = ">= 0.0.2"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

#Variable definition
#-----------------------------------------------------------
variable "aws_access_key_id" {
  type    = string
  default = "AKIA35FCJK2X5NOBGVZH"
}

variable "aws_secret_access_key" {
  type    = string
  default = "ImzrUZHjm1L2iDfREgRgEVTkICZglDSZy9zcCu7y"
}

variable "aws_profile" {
  type    = string
  default = "dev"
}

variable "ami_name" {
  type        = string
  description = "Name of the AMI"
  default     = "custom_AMI"
}

variable "aws_region" {
  type        = string
  description = "AWS_region"
  default     = "us-east-1"
}

variable "source_ami" {
  type        = string
  description = "Source AMI ID"
  default     = "ami-0dfcb1ef8550277af"
}

variable "ssh_username" {
  type        = string
  description = "Username of Instance"
  default     = "ec2-user"
}

variable "subnet_id" {
  type        = string
  description = "Subnet ID"
  default     = "subnet-0c6995c9f56334d32"
}

variable "ami_description" {
  type        = string
  description = "AMI description"
  default     = "custom AMI created"
}

variable "delete_on_termination" {
  type        = bool
  description = "Delete on Termination(True for yes)"
  default     = true
}

variable "vpc_id" {
  type        = string
  description = "Default VPC ID"
  default     = "vpc-0182444f5986c7503"
}

locals {
  timestamp = regex_replace(timestamp(), "[- TZ:]", "")
}

#Source
#--------------------------------------------------------------------
source "amazon-ebs" "my-ami" {
  region                  = "${var.aws_region}"
  ami_name                = "${var.ami_name}-${local.timestamp}"
  ami_description         = "${var.ami_description}"
  profile                 = "${var.aws_profile}"
  access_key              = "${var.aws_access_key_id}"
  secret_key              = "${var.aws_secret_access_key}"
  ssh_agent_auth          = false
  temporary_key_pair_type = "rsa"
  ami_regions = [
    "${var.aws_region}",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }
  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = "${var.delete_on_termination}"
    device_name           = "/dev/xvda"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

build {
  name = "Building custom AMI"
  sources = ["source.amazon-ebs.my-ami"]
  #  provisioner "file" {
  #    source      = ""
  #    destination = "/home"
  #  }
  #
  #  provisioner "file" {
  #    source      = ""
  #    destination = "/home"
  #  }

  provisioner "shell" {
    inline = [
      "touch shivani.txt"
    ]
  }

  post-processor "manifest" {
    output     = "manifest.json"
    strip_path = true
  }
}
