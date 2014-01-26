all.txt: lot1.txt lot2.txt lol.txt
	lot1.txt > all.txt; lot2.txt >> all.txt; lol.txt >> all.txt

lot1.txt:
	rm -f lot1.txt; for i in {0..4}; do echo "lot1:"$i >> lot1.txt; done;

lot2.txt:
	rm -f lot2.txt; for i in {5..10}; do echo "lot2:"$i >> lot2.txt; done;
