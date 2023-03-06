all: task.c parreg.c main.c qsort.h
	gcc -fopenmp -O3 task.c main.c -o task
	gcc -fopenmp -O3 parreg.c main.c -o parreg

clean:
	rm -f *.o parreg task
