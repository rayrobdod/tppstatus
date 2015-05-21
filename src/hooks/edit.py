#!/path/to/python.exe
# coding=UTF-8

import os, sys
import cgi
import cgitb
cgitb.enable()
import msvcrt 
msvcrt.setmode(sys.stdout.fileno(), os.O_BINARY) 
import datetime
import subprocess
from wsgiref.headers import Headers
from urllib import unquote
import json


repoDirectory = '/path/to/tppStatus/'
gitPath = '/path/to/git'
sbtPath = '/path/to/sbt-launch.jar'
javaPath = '/path/to/java'
logPath = 'log'

dataDirectory = repoDirectory + '/src/main/assets/'
targetDirectroy = repoDirectory + '/target/web/stage/'
gitCommitCommand = [gitPath, "commit", "-a", "-m", "Data Update"]
gitPushCommand1 = [gitPath, "push", "git@github.com:rayrobdod/tppstatus.git", "master:master"]
gitPushCommand2 = [gitPath, "push", "git@github.com:rayrobdod/tppstatus.git", "gh-pages:gh-pages"]
rebuildCommand = [javaPath, "-jar", sbtPath, "web-stage"]




sentHeaders = Headers([])
cgiFieldStorage = cgi.FieldStorage()


sentHeaders.add_header("Content-type", "text/html; charset=utf-8")
requestUri = os.environ["REQUEST_URI"] if ("REQUEST_URI" in os.environ) else (sys.argv[1] if (len(sys.argv) > 1) else False)
doHeaders  = "REQUEST_URI" in os.environ

valid = False
if ("identifier" in cgiFieldStorage.keys()):
	import re
	s = cgiFieldStorage.getfirst("identifier")
	found_s = re.findall('^[\w-]+$', s)
	valid = bool(found_s) and found_s[0] == s
else:
	valid = False

if valid:
	dataLocation = dataDirectory + cgiFieldStorage.getfirst("identifier") + ".json"
	dataFile = open(dataLocation)
	data = json.load(dataFile)
	dataFile.close()

	for key in cgiFieldStorage.keys():
		if key != "authToken" and key != "identifier":
			keyParts = key.split('_')
			
			if 1 == len(keyParts):
				data[key] = cgiFieldStorage.getfirst(key)
			if 3 == len(keyParts):
				dataList = data[keyParts[0]]
				if len(dataList) <= int(keyParts[1]):
					dataList.append({})
				dataValue = dataList[int(keyParts[1])]
				dataValue[keyParts[2]] = cgiFieldStorage.getfirst(key)
				
			if 4 == len(keyParts):
				dataList = data[keyParts[0]]
				while len(dataList) <= int(keyParts[1]):
					dataList.append({})
				dataValue = dataList[int(keyParts[1])]
				if not keyParts[2] in dataValue:
					dataValue[keyParts[2]] = []
				dataList2 = dataValue[keyParts[2]]
				while len(dataList2) <= int(keyParts[3]):
					dataList2.append("")
				
				dataList2[int(keyParts[3])] = cgiFieldStorage.getfirst(key)
	
	
	dataFile = open(dataLocation, 'w')
	json.dump(data, dataFile, indent=4, separators=(',', ': '), sort_keys=True)
	dataFile.close()
	
	gitCommitProcess1 = subprocess.Popen(gitCommitCommand, cwd=repoDirectory, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	gitCommitProcess1Out, gitCommitProcess1Err = gitCommitProcess1.communicate()
	
	rebuildProcess = subprocess.Popen(rebuildCommand, cwd=repoDirectory, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=False)
	rebuildProcessOut, rebuildProcessErr = rebuildProcess.communicate()
	
	gitPushProcess1 = subprocess.Popen(gitPushCommand1, cwd=repoDirectory, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	gitPushProcess1Out, gitPushProcess1Err = gitCommitProcess1.communicate()
	
	gitCommitProcess2 = subprocess.Popen(gitCommitCommand, cwd=targetDirectroy, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	gitCommitProcess2Out, gitCommitProcess2Err = gitCommitProcess2.communicate()
	gitPushProcess2 = subprocess.Popen(gitPushCommand2, cwd=targetDirectroy, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	gitPushProcess2Out, gitPushProcess2Err = gitCommitProcess1.communicate()
	
	logFile = open(logPath, 'a')
	logFile.write("\n\n")
	logFile.write('-' * 60 + "\n")
	logFile.write(gitCommitProcess1Out)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitCommitProcess1Err)
	logFile.write('-' * 20 + "\n")
	logFile.write(rebuildProcessOut)
	logFile.write('-' * 20 + "\n")
	logFile.write(rebuildProcessErr)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitPushProcess1Out)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitPushProcess1Err)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitCommitProcess2Out)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitCommitProcess2Err)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitPushProcess2Out)
	logFile.write('-' * 20 + "\n")
	logFile.write(gitPushProcess2Err)
	logFile.write('-' * 20 + "\n")
	
	
	
	if doHeaders :
		sys.stdout.write( str(sentHeaders) )
	
	print "<html><p>Accepted</p><pre>"
	print "</pre></html>"
else:
	sentHeaders.add_header("Status", "400 Bad Request")
	
	if doHeaders :
		sys.stdout.write( str(sentHeaders) )
	
	print """<html><p>This is an application endpoint.</p></html>"""
