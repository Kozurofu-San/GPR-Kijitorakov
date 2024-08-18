chcp 65001 | out-null

Set-Location ..
$folders = 'build', 'build-debug', 'build-release'
foreach($f in $folders){
    if(Test-Path $f){
        Write-Host 'Removed folder' $f
        Remove-Item $f -Recurse -Force -Confirm:$false
    }
}
if (-not(Test-Path -Path 'dependencies')) {
    exit
}
Set-Location ./dependencies
$ls = Get-ChildItem -Name
foreach($i in $ls){
    # if(git check-ignore $i){
        Write-Host 'Removed' $i
        Remove-Item $i -Recurse -Force -Confirm:$false
    # }
}