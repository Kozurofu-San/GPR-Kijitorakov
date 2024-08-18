chcp 65001 | out-null

cd ..
$folders = 'build', 'build-debug', 'build-release'
foreach($f in $folders){
    if(Test-Path $f){
        Write-Host 'Removed folder' $f
        Remove-Item $f -Recurse -Force -Confirm:$false
    }
}

# cd ./subprojects
# $ls = Get-ChildItem -Name
# git check-ignore stw_sat.wrap
# foreach($i in $ls){
#     if(git check-ignore $i){
#         Write-Host 'Removed' $i
#         Remove-Item $i -Recurse -Force -Confirm:$false
#     }
# }

############################## BATCH SCRIPT ##########################
# @echo off
# chcp 1251
# SetLocal EnableExtensions

# cd ..
# rmdir /s /q build-debug
# rmdir /s /q build-release
# rmdir /s /q build

# cd ./subprojects
# for /d %%i in (*) do (
#     for /f %%s in ('git check-ignore %%i') do rmdir /s /q %%s
# )

# for %%i in (*) do (
#     for /f %%s in ('git check-ignore %%i') do del %%s
# )