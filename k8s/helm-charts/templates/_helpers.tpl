{{/*
Expand the name of the chart.
*/}}
{{- define "ecommerce.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "ecommerce.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ecommerce.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "ecommerce.labels" -}}
helm.sh/chart: {{ include "ecommerce.chart" . }}
{{ include "ecommerce.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "ecommerce.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ecommerce.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Blue-Green deployment slot
*/}}
{{- define "ecommerce.slot" -}}
{{- if .Values.bluegreen.enabled }}
{{- .Values.bluegreen.activeSlot }}
{{- else }}
{{- "active" }}
{{- end }}
{{- end }}

{{/*
Create service name with slot suffix
*/}}
{{- define "ecommerce.serviceName" -}}
{{- $serviceName := .serviceName -}}
{{- if .Values.bluegreen.enabled }}
{{- printf "%s-%s" $serviceName (include "ecommerce.slot" .) }}
{{- else }}
{{- $serviceName }}
{{- end }}
{{- end }}

{{/*
Environment variables for microservices
*/}}
{{- define "ecommerce.envVars" -}}
- name: SPRING_PROFILES_ACTIVE
  value: "kubernetes"
- name: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE
  value: "http://{{ include "ecommerce.serviceName" (dict "serviceName" "discovery-service" "Values" .Values) }}:8761/eureka"
- name: SPRING_CLOUD_CONFIG_URI
  value: "http://{{ include "ecommerce.serviceName" (dict "serviceName" "config-server" "Values" .Values) }}:8888"
{{- if .Values.kafka.enabled }}
- name: SPRING_KAFKA_BOOTSTRAP_SERVERS
  value: "{{ .Release.Name }}-kafka:9092"
{{- end }}
{{- if .Values.mongodb.enabled }}
- name: SPRING_DATA_MONGODB_URI
  value: "mongodb://admin:admin123@{{ .Release.Name }}-mongodb:27017/ecommerce?authSource=admin"
{{- end }}
{{- if .Values.postgresql.enabled }}
- name: SPRING_DATASOURCE_URL
  value: "jdbc:postgresql://{{ .Release.Name }}-postgresql:5432/ecommerce"
- name: SPRING_DATASOURCE_USERNAME
  value: "ecommerce"
- name: SPRING_DATASOURCE_PASSWORD
  value: "ecommerce123"
{{- end }}
{{- end }}
