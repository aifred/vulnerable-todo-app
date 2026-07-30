if (-not (Get-Command sonar -ErrorAction SilentlyContinue)) {
    exit 0
}
$stdinData = [Console]::In.ReadToEnd()
$stdinData | & sonar hook claude-post-tool-use --project 'aifred_vulnerable-todo-app'
exit $LASTEXITCODE
