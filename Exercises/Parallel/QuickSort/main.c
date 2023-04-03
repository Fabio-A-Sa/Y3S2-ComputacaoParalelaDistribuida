#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <omp.h>
#include "qsort.h"

int floatcompare(const void *p1, const void *p2)
{
    float i = *((float *)p1);
    float j = *((float *)p2);
    if (i > j) return (1);
    if (i < j) return (-1);
    return (0);
}

int partition (int p, int r, float *data)
{
    float x = data[p];
    int k = p;
    int l = r+1;
    float t;
    while (1) {
        do k++; while ((data[k] <= x) && (k < r));
        do l--; while (data[l] > x);
        while (k < l) {
            t = data[k];  data[k] = data[l];  data[l] = t;
            do k++; while (data[k] <= x);
            do l--; while (data[l] > x);
        }
        t = data[p];  data[p] = data[l];  data[l] = t;
        return l;
    }
}

int main(int argc, char* argv[])
{
    int i,  n, low_limit;
    float *data, *databak;
    time_t start, end;

    /*
     * Get input
     */
    if (argc<3) {
        printf("Usage: a.out number low_limit\n");
        exit(1);
    }
    n = atoi(argv[1]);
    low_limit = atoi(argv[2]);
    
    /*
     * Generate the array
     */
    data = (float *) malloc (sizeof(float)*n);
    databak = (float *) malloc (sizeof(float)*n);
    for (i=0; i<n; i++) {
        databak[i] = data[i] = 1.1 * rand() * 5000 / RAND_MAX;
    }
    /* Create an imbalanced sort tree */
    databak[0] = 1; 
    
    /*
     * Quick sort using OMP Task
     */
    omp_set_dynamic (0);
    omp_set_nested (1);
    time(&start);
    par_quick_sort (n-1, &data[0], low_limit);
    time(&end);
    printf("Parallel quick_sort() Time: %f s\n", difftime(end,start));
    return 0;
}
