cd C:\workspace_peter.javaeclipse\reg_homework\sample_data
$folderPath = 'C:\workspace_peter.javaeclipse\reg_homework\sample_data' 
 
Write-Host "Executing Script..." 
 
foreach ($file in Get-ChildItem $folderPath -file)
{
       Start-Sleep -Seconds 1.5
	   curl -X PUT -H "Content-Type: application/json" -d @$file http://localhost:8080/timeseries/upload 
	   write-host ""
}

cd ..



