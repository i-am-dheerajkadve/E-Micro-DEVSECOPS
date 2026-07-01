{{- define "app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "app.fullname" -}}
{{- default (include "app.name" .) .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}
