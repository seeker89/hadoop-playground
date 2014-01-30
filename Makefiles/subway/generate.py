#!/usr/bin/python
__author__ = 'vivek'
import sys, getopt
 
ifile=''
ofile='out.mpg'
format='1280x720'
frames='1'

###############################
# o == option
# a == argument passed to the o
###############################
# Cache an error with try..except 
# Note: options is the string of option letters that the script wants to recognize, with 
# options that require an argument followed by a colon (':') i.e. -i fileName
#
try:
    myopts, args = getopt.getopt(sys.argv[1:],"i:o:f:n:")
except getopt.GetoptError as e:
    print (str(e))

for o, a in myopts:
    if o == '-i':
        ifile=a
    elif o == '-o':
        ofile=a
    elif o == '-f':
        format=a
    elif o == '-n':
        frames=a

if ifile == '':
    print("Usage: %s -f input.blend -n num_frames -o output.mpg" % sys.argv[0])
    sys.exit(2)
 
# Display input and output file name passed as the args
print ("Input file : %s, output file: %s, frames: %s, format: %s" % (ifile,ofile,frames,format) )


f = open('Makefile','w')

frames_string = ""
for i in range(1, int(frames)+1):
    frames_string += "frame_" + str(i) + ".png "

# main goal
f.write('%s: %s\n' % (ofile, frames_string))
f.write('\tconvert %s -antialias -quality 95%% %s\n' % (frames_string, ofile))

# frames
for i in range(1, int(frames)+1):
    f.write('frame_%d.png: %s\n' % (i, ifile))
    f.write('\tblender -b %s -o //frame_# -F PNG -x 1 -f %d\n' % (ifile,i))

# close the file
f.close() 
