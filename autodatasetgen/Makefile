all: autodatasetgen

autodatasetgen: autodatasetgen.6
	6l -o autodatasetgen autodatasetgen.6

autodatasetgen.6: autodatasetgen.go credentials.go
	6g -o autodatasetgen.6 autodatasetgen.go credentials.go

clean:
	rm -f *.6 autodatasetgen *~
