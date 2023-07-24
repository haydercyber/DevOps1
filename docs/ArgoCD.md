# Argo CD 

is a declarative continuous delivery (CD) tool for Kubernetes. It helps automate the deployment and lifecycle management of applications in Kubernetes clusters. Argo CD uses a GitOps approach, where the desired state of the applications is defined in a Git repository and automatically synchronized with the target cluster.
Key features and benefits of Argo CD include:
* Declarative GitOps: Argo CD uses a declarative approach, where the desired state of applications and configurations is defined in Git repositories as version-controlled manifests. Argo CD continuously monitors the repository and ensures that the cluster matches the desired state.
* Automated Sync: Argo CD automatically compares the current state of the cluster with the desired state defined in the Git repository. If any differences are detected, it reconciles the changes by deploying or updating the necessary resources in the cluster.
* Multi-Environment Support: Argo CD supports deploying applications to multiple environments, such as development, staging, and production. It allows you to define different target environments and manage their configurations separately.
* Rollback and Rollout Tracking: Argo CD provides rollback capabilities, allowing you to easily revert to a previous known-good state if needed. It also tracks the history of deployments, making it easy to view the progress and status of each rollout.
* Application Lifecycle Management: Argo CD manages the full lifecycle of applications, including deployment, scaling, and health monitoring. It provides a centralized interface to manage applications across clusters.
* Integration with CI/CD Pipelines: Argo CD can be integrated with CI/CD pipelines to automate the deployment process. You can trigger deployments based on Git commits or other pipeline events, ensuring a smooth workflow from development to production.
* Extensible and Customizable: Argo CD is highly extensible and customizable. It provides an API and hooks to integrate with external tools and extend its functionality to meet specific requirements.


Argo CD simplifies the management of Kubernetes applications by enforcing best practices, providing visibility into the application state, and reducing the complexity of manual deployments. It promotes GitOps principles, enabling teams to adopt a unified and efficient approach to application delivery in Kubernetes environments.

![argocd](/assets/argocd.gif)

# Applying ArgoCD 
To Apply CD USING ArgoCD we should apply this points 
## `Applications` 
An application in Argo CD represents a set of Kubernetes resources that are deployed and managed as a unit. It typically consists of manifests (such as YAML files) that define the desired state of the application in a Git repository. Argo CD continuously monitors the Git repository and ensures that the cluster matches the desired state defined in the repository. Applications can be automatically deployed, updated, and rolled back as needed.

The code snippet you provided is an example of an Argo CD Application manifest in YAML format. This manifest describes an application named piktochart-dev-web and specifies its configuration details. Let's break down the different sections of the manifest:
```
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: piktochart-dev-web
  namespace: argocd
  labels:
    creativeadvtech.ml/argo: app
    creativeadvtech.ml/project: piktochart
    creativeadvtech.ml/app: web
    creativeadvtech.ml/env: dev
```
* `apiVersion` and `kind` specify the API version and kind of the resource, which is an Argo CD Application.

* `metadata` section contains metadata for the application, including the `name`, `namespace`, and labels.

```
spec:
  project: clients-staging
  source:
    repoURL: 'git@github.com:haydercyber/Piktochart.git'
    path: piktochart
    targetRevision: master
    helm:
      releaseName: piktochart
      valueFiles:
        - values-dev.yaml
```
* `spec` section defines the desired state of the application.
    * `project` specifies the project in Argo CD where the application should be deployed.
    * `source` defines the source of the application manifests and its configuration.
        * `repoURL` specifies the URL of the Git repository containing the application manifests.
        * `path` specifies the path within the repository where the manifests are located.
        * `targetRevision` specifies the branch, tag, or commit ID to use as the target revision.
        * `helm` section is used for Helm-based applications.
        * `releaseName` specifies the release name for the Helm chart.
        * `valueFiles` specifies the value files to be used for customizing the Helm chart.
```
destination:
    server: 'https://CLUSTERID.gr7.eu-central-1.eks.amazonaws.com'
    namespace: piktochart
```
* `destination` section specifies the target environment where the application will be deployed.
    * `server` specifies the Kubernetes cluster API server URL.
    * `namespace` specifies the target namespace where the application will be deployed.
```
syncPolicy:
    automated:
      prune: false
      selfHeal: false
      allowEmpty: false
```
* `syncPolicy` defines the synchronization policy for the application.
    * `prune` specifies whether to remove resources that are no longer defined in the Git repository.
    * `selfHeal` specifies whether to automatically fix resources that have drifted from the desired state.
    * `allowEmpty` specifies whether to allow syncing an empty directory.

This Argo CD Application manifest describes the desired state of the application, its source repository, target environment, and synchronization policy. When applied to Argo CD, it will deploy and manage the application based on the defined configuration.
lets check if it ok or notes 
```
helm template . 
```
the output 
```
---
# Source: applications/templates/Piktochart/dev/piktochart.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: piktochart-dev-web
  namespace: argocd
  labels:
    creativeadvtech.ml/argo: app
    creativeadvtech.ml/project: piktochart
    creativeadvtech.ml/app: web
    creativeadvtech.ml/env: dev
spec:
  project: clients-staging
  source:
    repoURL: 'git@github.com:haydercyber/Piktochart.git'
    path: piktochart
    targetRevision: master
    helm:
      releaseName: piktochart
      valueFiles:
        - values-dev.yaml
  destination:
    server: 'https://CLUSTERID.gr7.eu-central-1.eks.amazonaws.com'
    namespace: piktochart
  syncPolicy:
    automated:
      prune: false
      selfHeal: false
      allowEmpty: false
```
its ok 

## Clusters 
Clusters in Argo CD represent the target Kubernetes environments where applications are deployed. Each cluster is configured with connection details, such as the API server address, authentication credentials, and context. Argo CD can manage multiple clusters, allowing you to deploy applications to different environments, such as development, staging, and production. Clusters can be registered in Argo CD to enable synchronization and deployment of applications to those environments

The code snippet you provided is an example of a Kubernetes Secret manifest in YAML format. This Secret is named "piktochart-staging" and contains sensitive data that will be stored securely in the Kubernetes cluster. Let's break down the different sections of the manifest:
```
apiVersion: v1
kind: Secret
metadata:
  annotations:
    {{- with .Values.annotationsCommon }}
    {{ toYaml . | indent 4 }}
    {{- end }}
  labels:
    account-id: "123113232352"
    argocd.argoproj.io/secret-type: "cluster"
    account-name: "piktochart"
    account-type: "staging"
    region: "eu-central-1"
    cluster-type: "external"
    k8s-version: "1.25"
  name: piktochart-staging
type: Opaque
stringData:
  config: |
    {
      "bearerToken": "{{ index .Values "piktochart" "staging" "bearerToken" }}",
      "tlsClientConfig": {
        "insecure": false,
        "caData": "caData"
      }
    }
```
* `apiVersion` and kind specify the API version and kind of the resource, which is a Secret.
* `metadata` section contains metadata for the Secret, including the   `name`, `annotations`, and `labels`.
    * `annotations` are used to provide additional information or metadata about the `Secret`. In this case, the `annotationsCommon` values are rendered using the toYaml function and indented by four spaces.
    * `labels` provide key-value paairs to label the Secret for easy identification and grouping.
* `name` specifies the name of the Secret, which is `"piktochart-staging"`.
* `type` indicates the type of the Secret, which is `"Opaque"` in this case, meaning it is a generic Secret that can hold arbitrary data.
* `stringData` section is used to store `string-based` data in the Secret. In this case, it contains a single key-value pair with the key "config" and the value defined as a multi-line string using the | `symbol`.
* The value of the `"config"` key is a `JSON-formatted` string, where the `"bearerToken"` value is rendered using the Helm template function index to access a specific value from the Values object. The `"tlsClientConfig"` section contains hardcoded values for demonstration purposes.
Please note that the values inside the Secret manifest are placeholders and should be replaced with actual sensitive data when deploying the Secret to your Kubernetes cluster.
lets do the test 
```
$ helm template .
---
# Source: clusters/templates/Piktochart-staging.yaml
apiVersion: v1
kind: Secret
metadata:
  annotations:
    created-by: infrastructure/Piktochart/argocd/clusters
    managed-by: argocd.argoproj.io
  labels:
    account-id: "123113232352"
    argocd.argoproj.io/secret-type: "cluster"
    account-name: "piktochart"
    account-type: "staging"
    region: "eu-central-1"
    cluster-type: "external"
    k8s-version: "1.25"
  name: piktochart-staging
type: Opaque
stringData:
  config: |
    {
      "bearerToken": "$bearerToken",
      "tlsClientConfig": {
        "insecure": false,
        "caData": "caData"
      }
    }
```
# Projects 
Projects in Argo CD provide a way to logically group applications and manage their access controls. Projects define a boundary for managing applications and help with role-based access control (RBAC) within Argo CD. You can define policies, permissions, and synchronization settings at the project level. Projects enable teams to organize and manage their applications effectively, especially in multi-tenant or multi-team environments. By organizing applications into projects and deploying them to specific clusters, Argo CD provides a centralized and scalable way to manage application deployments across Kubernetes environments. It allows for fine-grained control over access, synchronization, and rollbacks. With Argo CD, you can have a clear separation of environments, manage application configurations declaratively, and automate the deployment process, promoting consistency and reliability in your CD workflows. 

The code snippet you provided is an example of an Argo CD AppProject manifest in YAML format. An AppProject represents a project within Argo CD and allows you to organize and manage applications within a specific context. Let's break down the different sections of the manifest: 

```
{{- $name := .Values.projects.clientsstaging.name -}}
{{- if .Values.projects.clientsstaging.enabled -}}
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: {{ $name }}
spec:
  clusterResourceWhitelist:
  - group: '*'
    kind: '*'
  description: Dashboard Portal for clients Staging
  destinations:
  - name: ' clientsstaging'
    namespace: '*'
    server: {{ .Values.projects.clientsstaging.cluster }}
  roles:
  - description: DevOps Team who added GitHub argocd-admins group.
    groups:
    - piktochart/argocd-admins
    name: devops
    policies:
    - p, proj:{{ $name }}:devops, applications, *, {{ $name }}/*, allow
  sourceRepos:
  {{- toYaml  .Values.projects.clientsstaging.sourceRepos | nindent 4 }}
status:
  jwtTokensByRole:
    devops: {}
{{- end -}}
```
* The first line defines a variable `$name` using the value from `.Values.projects.clientsstaging.name.` This allows you to reuse the project name throughout the manifest.
* The if `.Values.projects.clientsstaging.enabled` condition checks if the project is enabled before generating the manifest.
* `apiVersion` and kind specify the API version and kind of the resource, which is an Argo CD AppProject.
* `metadata` section contains metadata for the AppProject, including the name.
* `spec` section defines the desired state of the AppProject.
    * `clusterResourceWhitelist` allows all resources from any group and kind in the cluster.
    * `description` provides a description for the AppProject.
    * `destinations` specify the target environments for the AppProject.
    * `roles` define the roles within the project, including their descriptions, groups, and policies.
    * `sourceRepos` specify the allowed source repositories for the AppProject.
* `status` section defines the status of the AppProject, such as JWT tokens by role.
The AppProject manifest provides a way to define and configure a project within Argo CD, including its destinations, roles, source repositories, and more. This allows you to manage and organize applications within the project and control access and policies for specific roles or teams.
lets see the is everything ok 

```
helm template .
---
# Source: projects/templates/clients-staging.yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: clients-staging
spec:
  clusterResourceWhitelist:
  - group: '*'
    kind: '*'
  description: Dashboard Portal for clients Staging
  destinations:
  - name: ' clientsstaging'
    namespace: '*'
    server: https://CLUSTERID.gr7.eu-central-1.eks.amazonaws.com
  roles:
  - description: DevOps Team who added GitHub argocd-admins group.
    groups:
    - piktochart/argocd-admins
    name: devops
    policies:
    - p, proj:clients-staging:devops, applications, *, clients-staging/*, allow
  sourceRepos:
    - https://charts.bitnami.com/bitnami
    - https://github.com/haydercyber/Piktochart.git
status:
  jwtTokensByRole:
    devops: {}
```

Next: [CI/CD](CICD.md)