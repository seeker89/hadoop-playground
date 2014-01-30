all.txt: sleep1.txt sleep2.txt sleep3.txt sleep4.txt sleep5.txt sleep6.txt sleep7.txt sleep8.txt
	cat sleep1.txt > all.txt; cat sleep2.txt >> all.txt; cat sleep3.txt >> all.txt; cat sleep4.txt >> all.txt; cat sleep5.txt >> all.txt; cat sleep6.txt >> all.txt; cat sleep7.txt >> all.txt; cat sleep8.txt >> all.txt;

sleep1.txt:
	rm -f sleep1.txt; for i in {1..10}; do uname -a >> sleep1.txt; sleep 1; done;

sleep2.txt:
	rm -f sleep2.txt; for i in {1..10}; do uname -a >> sleep2.txt; sleep 1; done;

sleep3.txt:
	rm -f sleep3.txt; for i in {1..10}; do echo ${PATH}" : "${i} >> sleep3.txt; sleep 1; done;

sleep4.txt:
	rm -f sleep4.txt; for i in {1..10}; do echo ${PATH}" : "${i} >> sleep4.txt; sleep 1; done;


sleep5.txt:
	rm -f sleep5.txt; for i in {1..10}; do uname -a >> sleep5.txt; sleep 1; done;

sleep6.txt:
	rm -f sleep6.txt; for i in {1..10}; do uname -a >> sleep6.txt; sleep 1; done;

sleep7.txt:
	rm -f sleep7.txt; for i in {1..10}; do echo ${PATH}" : "${i} >> sleep7.txt; sleep 1; done;

sleep8.txt:
	rm -f sleep8.txt; for i in {1..10}; do echo ${PATH}" : "${i} >> sleep8.txt; sleep 1; done;


clean:
	rm all.txt sleep1.txt sleep2.txt sleep3.txt sleep4.txt sleep5.txt sleep6.txt sleep7.txt sleep8.txt
	
