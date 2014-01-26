all.txt: dep1_1.txt dep1_2.txt
	cat dep1_1.txt > all.txt; cat dep1_2.txt >> all.txt;

dep1_1.txt: dep2_1.txt
	echo "I am dep1_1, my child has this to say:" > dep1_1.txt; cat dep2_1.txt >> dep1_1.txt

dep1_2.txt: dep2_2.txt
	echo "I am dep1_2, my child has this to say:" > dep1_2.txt; cat dep2_2.txt >> dep1_2.txt

dep2_1.txt:
	echo "    Hello, I'm a leaf! My name is dep2_1" > dep2_1.txt

dep2_2.txt:
	echo "    To be a leaf or not to be... that's a question without a mark. --dep2_2" > dep2_2.txt
