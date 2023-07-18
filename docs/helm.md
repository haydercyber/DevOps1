# Create helm chart 
create a Helm chart for a Ruby app that has been Dockerized, you can 
```
# helm create piktochart  
```
This will create a new directory named  `piktochart` with the basic structure of a Helm chart. 
The updated `Chart.yaml` for the piktochart Helm chart would look like this

```
apiVersion: v2
name: piktochart
description: A Helm chart for Kubernetes

# A chart can be either an 'application' or a 'library' chart.
#
# Application charts are a collection of templates that can be packaged into versioned archives
# to be deployed.
#
# Library charts provide useful utilities or functions for the chart developer. They're included as
# a dependency of application charts to inject those utilities and functions into the rendering
# pipeline. Library charts do not define any templates and therefore cannot be deployed.
type: application

# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
# Versions are expected to follow Semantic Versioning (https://semver.org/)
version: 0.1.0

# This is the version number of the application being deployed. This version number should be
# incremented each time you make changes to the application. Versions are not expected to
# follow Semantic Versioning. They should reflect the version the application is using.
# It is recommended to use it with quotes.
appVersion: "1.16.0"

dependencies:
- name: redis
  condition: redis-cache.enabled
  alias: redis-cache
  version: "17.11.8"
  repository: https://charts.bitnami.com/bitnami
```
In this example, the `Chart.yaml` includes the name, description, type, version, and appVersion fields. It also includes a dependencies section, where you can specify any dependencies required for your application. In this case, there is a dependency on a Redis chart from the Bitnami repository.

You can customize the fields according to your specific application and requirements. Make sure to update the name, description, version, and appVersion fields appropriately. If you have any additional dependencies, you can include them in the dependencies section.

the dependencies section is used to specify any dependencies required by your application. These dependencies can be other Helm charts that need to be deployed alongside your main application.

Let's take a closer look at the dependencies section in the example

```
dependencies:
- name: redis
  condition: redis-cache.enabled
  alias: redis-cache
  version: "17.11.8"
  repository: https://charts.bitnami.com/bitnami
```
In this example, there is a single dependency specified
* `name` This field specifies the name of the dependency. In this case, the dependency is named redis.
* `condition` The condition field specifies a condition that determines whether the dependency should be enabled or disabled. Here, the redis-cache.enabled condition is used. It means that the dependency will only be included if the redis-cache value in the values.yaml file is set to true.
* `alias` The alias field provides an alias for the dependency. It allows you to refer to the dependency by a different name within your chart. In this example, the redis-cache alias is used to refer to the Redis dependency.
* `version` The version field specifies the version of the dependency chart that should be used. In this case, the Redis dependency should be at version 17.11.8.
* `repository` The repository field specifies the repository URL from which to fetch the dependency chart. In this example, the Redis dependency is fetched from the Bitnami charts repository at https://charts.bitnami.com/bitnami.

By including these dependency details in the `Chart.yaml` file, Helm will automatically manage the deployment of both your main application and its dependencies when the chart is installed.
You can add additional dependencies to the dependencies section as needed, following the same structure. Each dependency can have its own condition, alias, version, and repository information.
Remember to update the dependency details based on the actual dependencies required by your application.

```
helm dep list
```
This command lists the dependencies of a Helm chart. It displays the names and versions of the chart dependencies defined in the Chart.yaml file. To use this command, navigate to the directory containing your Helm chart and run the following command

```
helm dep update
```
This command updates the dependencies of a Helm chart. It resolves and downloads the latest versions of the chart dependencies based on the version constraints specified in the `Chart.yaml` file. To update the dependencies, navigate to the directory containing your Helm chart and run the following command

i use default chart  i just update the configmap to use at as env 
The code snippet you provided appears to be a Kubernetes YAML manifest file defining a ConfigMap resource. The ConfigMap contains key-value pairs that can be used as environment variables or configuration data in your Kubernetes deployments.

Here's a breakdown of the YAML file:
```
apiVersion: v1
kind: ConfigMap
metadata:
  name:  {{ include "piktochart.fullname" . }}
data:
  RAILS_ENV:              "{{ .Values.config.RAILS_ENV }}"
  RACK_ENV:               "{{ .Values.config.RACK_ENV }}"
  RAILS_MAX_THREADS:      "{{ .Values.config.RAILS_MAX_THREADS }}"
  REDIS_URL:              "{{ .Values.config.REDIS_URL }}"
  PIDFILE:                "{{ .Values.config.PIDFILE }}"
  CONFIG_HOSTS:           "{{ .Values.config.CONFIG_HOSTS }}"
```
* `apiVersion` v1 specifies the Kubernetes API version for the ConfigMap resource.
* `kind` ConfigMap indicates that this YAML defines a ConfigMap resource.
* `metadata` contains metadata information for the ConfigMap, such as the name. In this case, the name is defined using the Helm template function include to reference the piktochart.fullname value from the Helm chart.
* `data` is a key-value section where you define the configuration data for your application. Each key-value pair represents an environment variable or a configuration value. The values are defined using the Helm template syntax, enclosed in double curly braces ({{ .Values.config.SOME_KEY }}). The . represents the context, and Values.config.SOME_KEY refers to the value of SOME_KEY under the config section in the Helm values.yaml file.

You can customize the values of `RAILS_ENV`, `RACK_ENV`, `RAILS_MAX_THREADS`, `REDIS_URL`, `PIDFILE`, and `CONFIG_HOSTS` by modifying the corresponding values in your Helm values.yaml file.
Once the ConfigMap is deployed, the keys and their associated values can be accessed by the pods in your Kubernetes cluster. You can mount the ConfigMap as environment variables or as files within your application containers, allowing your application to use the configuration data defined in the ConfigMap.


and i update the deploymenet to use this as env 


The envFrom section in the Kubernetes Deployment manifest is used to inject environment variables from a ConfigMap into the container running in the pod. In your provided YAML, the envFrom section references a ConfigMap using the Helm template function include to dynamically set the name of the ConfigMap.
Here's the snippet for reference:
```
envFrom:
    - configMapRef:
        name: {{ include "piktochart.fullname" . }}
```
In this example, the envFrom section specifies that environment variables should be injected from a ConfigMap. The `configMapRef` field is used to reference the ConfigMap.
The name of the ConfigMap is specified using the Helm template function include with the value piktochart.fullname. This means that the ConfigMap's name will be determined by the value of piktochart.fullname defined in your Helm chart's templates.
The `piktochart.fullname` value is typically derived from the name field in the metadata section of the Chart.yaml file or by combining other values specific to your chart. You can customize the logic for determining the piktochart.fullname value in your Helm chart's template files.
When the deployment is applied, the ConfigMap referenced by the name specified in piktochart.fullname will be used to inject the environment variables into the container running in the pod.
Make sure to define the corresponding ConfigMap in your Helm chart's templates or provide it externally when deploying the chart.
this the value for dev 
```
redis-cache:
  enabled: true
  architecture: standalone
  auth:
    enabled: false
    sentinal: true
  master:
    containerPorts:
      redis: 6379
    persistence:
      enabled: true
      storageClass: "ebs"
      accessModes:
        - ReadWriteOnce
      size: 8Gi
image:
  repository: 641616161.dkr.ecr.eu-central-1.amazonaws.com/piktochart
  pullPolicy: IfNotPresent
  # Overrides the image tag whose default is the chart appVersion.
  tag: "piktochart-248df10"
ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: nginx
    kubernetes.io/tls-acme: "true"
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: piktochart.dev.com
      paths:
        - path: /
          pathType: ImplementationSpecific
  tls:
    - secretName: warsha-ingress-tls
      hosts:
        - piktochart.dev.com
service:
  type: ClusterIP
  port: 3000
autoscaling:
  enabled: false
  minReplicas: 2
  maxReplicas: 4
  targetCPUUtilizationPercentage: 80
config:
  RAILS_ENV: development           
  RACK_ENV: development         
  RAILS_MAX_THREADS: "5"
  REDIS_URL:  redis://piktochart-redis-cache-master:6379 
  PIDFILE: tmp/pids/server.pid
  CONFIG_HOSTS: piktochart.dev.com
```
The snippet you provided appears to be a Helm chart's values.yaml file, which contains configuration values for the chart. It includes settings for the Redis cache, image, ingress, service, autoscaling, and application-specific configuration.
Let's break down the different sections and their meanings:
* Redis cache configuration:
   * `enabled`: Specifies whether the Redis cache is enabled or not.
   * `architecture`: Specifies the Redis cache architecture (in this case, standalone).
   * `auth`: Configures authentication for Redis (in this case, authentication is not enabled).
   * `sentinel`: Specifies whether Redis Sentinel is enabled or not.
   * `master`: Contains configuration specific to the Redis master instance, including container ports and persistence settings.
* Image configuration:
    * `repository`: Specifies the repository for the container image.
    * `pullPolicy`: Specifies the image pull policy (in this case,  IfNotPresent).
    * `tag`: Overrides the default image tag with a specific version (piktochart-248df10).
* Ingress configuration:
    * `enabled`: Specifies whether the Ingress resource is enabled or not.
    * `annotations`: Defines annotations for the Ingress resource, including ingress class, TLS options, and cluster issuer.
    * `hosts`: Specifies the hostname and paths for the Ingress resource.
    * `tls`: Specifies the TLS secret name and hosts for the Ingress resource.
* Service configuration:
    * `type`: Specifies the service type (in this case, ClusterIP).
    * `port`: Specifies the service port (in this case, 3000).
* Autoscaling configuration:
    * `enabled`: Specifies whether autoscaling is enabled or not.
    * `minReplicas`: Specifies the minimum number of replicas.
    * `maxReplicas`: Specifies the maximum number of replicas.
    * `targetCPUUtilizationPercentage`: Specifies the target CPU utilization percentage for autoscaling.
* Application-specific configuration:
    * `config`: Contains configuration values specific to the application.
The `RAILS_ENV`, `RACK_ENV`, `RAILS_MAX_THREADS`, `REDIS_URL`, `PIDFILE`, and `CONFIG_HOSTS` settings define environment variables used by the application.
You can customize these values based on your specific requirements by modifying the values.yaml file. These values will be used during the deployment of the Helm chart to configure the corresponding resources and components.

you can see the output logs 
```
Puma starting in single mode...
* Puma version: 5.6.5 (ruby 3.0.6-p216) ("Birdie's Version")
*  Min threads: 5
*  Max threads: 5
*  Environment: development
*          PID: 1
* Listening on http://0.0.0.0:3000
Use Ctrl-C to stop
```

in case we need to prepare new enviorment for prodution we can only change the `RAILS_ENV`, `RACK_ENV` 

lets see the output for produtions 
```
Puma starting in single mode...
* Puma version: 5.6.5 (ruby 3.0.6-p216) ("Birdie's Version")
*  Min threads: 5
*  Max threads: 5
*  Environment: produtions
*          PID: 1
* Listening on http://0.0.0.0:3000
Use Ctrl-C to stop
```

no need to have muttiple dockerfile only one dockerfile is ok 

Next: [ArgoCD](ArgoCD.md)