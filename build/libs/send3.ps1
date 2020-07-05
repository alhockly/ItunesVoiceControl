$port= new-Object System.IO.Ports.SerialPort COM3,9600,None,8,one
Try{
$port.open()
$port.WriteLine("3")
$port.close()
}Catch{
Write-Host "Eror"
}