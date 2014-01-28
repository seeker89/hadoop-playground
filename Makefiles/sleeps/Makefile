all.txt: sleep1.txt sleep2.txt sleep3.txt
	cat sleep1.txt > all.txt; cat sleep2.txt >> all.txt; cat sleep3.txt >> all.txt

sleep1.txt:
	rm -f sleep1.txt; for i in {1..30}; do echo `uname -a`" : "${i} >> sleep1.txt; sleep 1; done;

sleep2.txt:
	rm -f sleep2.txt; for i in {1..30}; do echo `uname -a`" : "${i} >> sleep2.txt; sleep 1; done;

sleep3.txt:
	rm -f sleep3.txt; for i in {1..30}; do echo ${PATH}" : "${i} >> sleep3.txt; sleep 1; done;

clean:
	rm all.txt sleep1.txt sleep2.txt sleep3.txt
	
