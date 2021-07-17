"""
In summary, I encountered three problems during execution, since I didn't commit frequently I will explain my develop
process here. The first commit is my first try of this assignment, as I needed to test the svn submission. I solved
the problems as I explained below.

Basically, from creating server socket to accepting client connection, the whole process is pretty straightforward. The
only problem I had was misunderstanding the data type of args in bind() so that I transferred the ip address into
integer in the first try.
Problems/efforts needed were in 1) reading cache file,  2) no cache for http 404 response, and 3) sending request to
origin server.

1) Since the response from origin server is cached in list, so I have to join it before reading it.

2) I grabbed the response header and stored it into a variable, so the program can be informed and thus skip caching
when the response is http 404 not found. And I moved cache_close() into the "if res not equal to 404" condition, so I
manage the cache operation in only one condition and a single part.

3) I made a silly mistake here as I first just directly forward the client request to origin server. Obviously, it
didn't work, since the host of request sent to origin server will be still localhost 8888. So I identified it's the
problem and I constructed the request by myself. It works.
"""

# Include the libraries for socket and system calls
import socket
import sys
import os
import argparse
import re

# 1MB buffer size
BUFFER_SIZE = 1000000

parser = argparse.ArgumentParser()
parser.add_argument('hostname', help='the IP Address Of Proxy Server')
parser.add_argument('port', help='the port number of the proxy server')
args = parser.parse_args()

# Create a server socket, bind it to a port and start listening
# The server IP is in args.hostname and the port is in args.port
# bind() accepts an integer only
# You can use int(string) to convert a string to an integer
# ~~~~ INSERT CODE ~~~~
host = ''
port = ''
# if the host is localhost
if args.hostname == 'localhost':
  host = '127.0.0.1'

else:
  host = gethostbyname(args.hostname)

port = args.port
port = int(port)
# ~~~~ END CODE INSERT ~~~~

try:
  # Create a server socket
  # ~~~~ INSERT CODE ~~~~
  proxy_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  # ~~~~ END CODE INSERT ~~~~
  print 'Connected socket'
except:
  print 'Failed to create socket'
  sys.exit()

try:
  # Bind the the server socket to a host and port
  # ~~~~ INSERT CODE ~~~~
  proxy_server.bind((host, port))
  # ~~~~ END CODE INSERT ~~~~
  print 'Port is bound'
except:
  print('Port is in use')
  sys.exit()

try:
  # Listen on the server socket
  # ~~~~ INSERT CODE ~~~~
  proxy_server.listen(5)
  # ~~~~ END CODE INSERT ~~~~
  print 'Listening to socket'
except:
  print 'Failed to listen'
  sys.exit()

while True:
  print 'Waiting connection...'

  clientSocket = None
  try:
    # Accept connection from client and store in the clientSocket
    # ~~~~ INSERT CODE ~~~~
    clientSocket, clientAddr = proxy_server.accept()
    # ~~~~ END CODE INSERT ~~~~
    print 'Received a connection from:', args.hostname
  except:
    print 'Failed to accept connection'
    sys.exit()

  message = 'METHOD URI VERSION'
  # Get request from client
  # and store it in message
  # ~~~~ INSERT CODE ~~~~
  message = clientSocket.recv(4096)
  # ~~~~ END CODE INSERT ~~~~

  print 'Received request:'
  print '< ' + message

  # Extract the parts of the HTTP request line from the given message
  requestParts = message.split()
  method = requestParts[0]
  URI = requestParts[1]
  version = requestParts[2]

  print 'Method:\t\t' + method
  print 'URI:\t\t' + URI
  print 'Version:\t' + version
  print ''

  # Remove http protocol from the URI
  URI = re.sub('^(/?)http(s?)://', '', URI, 1)

  # Remove parent directory changes - security
  URI = URI.replace('/..', '')

  # Split hostname from resource
  resourceParts = URI.split('/', 1)
  hostname = resourceParts[0]
  resource = '/'

  if len(resourceParts) == 2:
    # Resource is absolute URI with hostname and resource
    resource = resource + resourceParts[1]

  print 'Requested Resource:\t' + resource

  cacheLocation = './' + hostname + resource
  if cacheLocation.endswith('/'):
    cacheLocation = cacheLocation + 'default'

  print 'Cache location:\t\t' + cacheLocation

  fileExists = os.path.isfile(cacheLocation)
  
  try:
    # Check whether the file exist in the cache
    cacheFile = open(cacheLocation, "r")
    outputdata = cacheFile.readlines()

    print 'Cache hit! Loading from cache file: ' + cacheLocation
    # ProxyServer finds a cache hit
    # Send back contents of cached file
    # ~~~~ INSERT CODE ~~~~
    # since data from cached file is a list, need to convert it into string in order to send to client
    data = '\r\n'.join(outputdata)
    response = data
    clientSocket.sendall(response)
    # ~~~~ END CODE INSERT ~~~~

    cacheFile.close()
  # Error handling for file not found in cache
  except IOError:
    if fileExists:
      clientResponse = ''
      # If we get here, the file exists but the proxy can't open or read it
      # What would be the appropriate status code and message to send to client?
      # store the value in clientResponse
      # ~~~~ INSERT CODE ~~~~
      clientResponse = 'HTTP/1.1 500 Internal Server Error'
      # ~~~~ END CODE INSERT ~~~~

      print 'Sending to the client:'
      print '> ' + clientResponse
      print '>'
      clientSocket.sendall(clientResponse + "\r\n\r\n")

    else:
      originServerSocket = None
      # Create a socket to connect to origin server
      # and store in originServerSocket
      # ~~~~ INSERT CODE ~~~~
      originServerSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      # ~~~~ END CODE INSERT ~~~~

      print 'Connecting to:\t\t' + hostname + '\n'
      try:
        # Get the IP address for a hostname
        address = socket.gethostbyname(hostname)
        # Connect to the origin server
        # the port number of server is default 80
        # ~~~~ INSERT CODE ~~~~
        originServerSocket.connect((address, 80))
        # ~~~~ END CODE INSERT ~~~~

        print 'Connected to origin Server'

        # Create a file object associated with this socket
        # This lets us use file function calls
        originServerFileObj = originServerSocket.makefile('+', 0)

        originServerRequest = ''
        originServerRequestHeader = ''
        # Create origin server request line and headers to send
        # and store in originServerRequestHeader and originServerRequest
        # originServerRequest is the first line in the request and
        # originServerRequestHeader is the second line in the request
        # ~~~~ INSERT CODE ~~~~
        # HANDLE the case if request resources is like http://localhost:8888/http://autoidlab.cs.adelaide.edu.au/....png
        if len(resourceParts) == 2:
         originServerRequest = 'GET /' + resourceParts[1] + ' HTTP/1.1'
        else:
         originServerRequest = 'GET / HTTP/1.1'

        originServerRequestHeader = 'Host: ' + hostname + '\r\n' + 'Accept: text/html,image/png; charset=UTF-8\r\nKeep-Alive: 100\r\nConnection: keep-alive'

        # ~~~~ END CODE INSERT ~~~~

        # Construct the request to send to the origin server
        request = originServerRequest + '\r\n' + originServerRequestHeader + '\r\n\r\n'

        # Request the web resource from origin server
        print 'Forwarding request to origin server:'
        for line in request.split('\r\n'):
          print '> ' + line

        try:
          originServerSocket.sendall(request)
        except socket.error:
          print 'Send failed'
          sys.exit()

        originServerFileObj.write(request)

        # Get the response from the origin server
        # ~~~~ INSERT CODE ~~~~
        origin_server_res = ''

        # Keep receiving data from origin server until all data are received
        while True:
          data = originServerSocket.recv(4096)
          origin_server_res = origin_server_res + data

          if not data:
            break

        # get response header, to determine whether the response id http 404
        res_list = origin_server_res.split('\r\n')
        res_header = res_list[0]

        # ~~~~ END CODE INSERT ~~~~

        # Send the response to the client
        # ~~~~ INSERT CODE ~~~~
        clientSocket.sendall(origin_server_res)
        # ~~~~ END CODE INSERT ~~~~

        # finished sending to origin server - shutdown socket writes
        originServerSocket.shutdown(socket.SHUT_WR)

        print 'Request sent to origin server\n'

        # if the response is not HTTP 404 not found
        # Create a new file in the cache for the requested file.
        # Also send the response in the buffer to client socket
        # and the corresponding file in the cache
        # else the proxy doesn't cache 404 response
        if res_header != 'HTTP/1.1 404 Not Found':
          cacheDir, file = os.path.split(cacheLocation)
          print 'cached directory ' + cacheDir
          if not os.path.exists(cacheDir):
            os.makedirs(cacheDir)
          cacheFile = open(cacheLocation, 'wb')

        # Save origin server response in the cache file
        # then close
        # ~~~~ INSERT CODE ~~~~
          cacheFile.write(origin_server_res)
          cacheFile.close()
        # ~~~~ END CODE INSERT ~~~~

        print 'done sending'
        originServerSocket.close()
        print 'cache file closed'
        clientSocket.shutdown(socket.SHUT_WR)
        print 'client socket shutdown for writing'
      except IOError, (value, message):
        print 'origin server request failed. ' + message
  try:
    clientSocket.close()
  except:
    print 'Failed to close client socket'
