#!/usr/bin/groovy
package com.vizuri.openshift


def call(body) {
	def steps = new com.vizuri.openshift.PipelineSteps();
	def pipelineParams= [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = pipelineParams
	body()

	pipeline {
		environment { 
			RELEASE_NUMBER = ""; 			
			OCP_APP_SUFFIX = "";
			IMAGE_BASE = "";
			IMAGE_NAMESPACE = "";
			REGISTRY_USERNAME = "";
			REGISTRY_PASSWORD = "";			
			CONTAINER_REGISTRY = ""
			NEXUS_URL = ""	
		}
		node ("maven-podman") {
			steps.setEnv(pipelineParams);
			def projectFolder;
			if(pipelineParams.project_folder) {
				echo "setting project_folder: ${pipelineParams.project_folder}"
				projectFolder = pipelineParams.project_folder
			}
			else {
				echo "setting project_folder: default"
				projectFolder = "."
			}
			steps.checkout()
			
			try {
				steps.buildJava(projectFolder)
				steps.deployJava(projectFolder)
			} catch (e) {
				currentBuild.result = "FAILED"
				throw e
			} finally {
				//steps.notifyBuild(currentBuild.result)
			}
		}
	}
}