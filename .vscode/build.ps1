param([string] $Config)
$build_folder = 'build'
if (-not(Test-Path -Path $build_folder)) {
    mkdir $build_folder | Out-Null
    Write-Host Build folder is created
}
$dir = Get-Location
Set-Location $build_folder
# Get-Location
# Write-Host $dir
cmake -G Ninja $dir
cmake --build . --config Debug
Set-Location $dir
