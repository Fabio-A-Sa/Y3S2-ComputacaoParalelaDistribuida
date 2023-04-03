#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <omp.h>

static int
floatcompare(const void *p1, const void *p2)
{
    float i = *((float *)p1);
    float j = *((float *)p2);
    if (i > j) return (1);
    if (i < j) return (-1);
    return (0);
}

static int 
partition (int p, int r, float *data)
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

static void
seq_quick_sort (int p, int r, float *data)
{
    if (p < r) {
        int q = partition (p, r, data);
        seq_quick_sort (p, q-1, data);
        seq_quick_sort (q+1, r, data);
    }
}

static void
par_quick_sort (int p, int r, float *data, int low_limit)
{
    if (p < r) {
        if ((r-p) < low_limit) 
            seq_quick_sort (p, r, data);
        else {
            int q = partition (p, r, data);
            #pragma omp task
            par_quick_sort (p, q-1, data, low_limit);
            #pragma omp task
            par_quick_sort (q+1, r, data, low_limit);
        }
    }
}

int 
main(int argc, char* argv[])
{
    int i;
    int pthr;
    float* Data, *Databak1, *Databak2;
    int    N, low_limit;
    hrtime_t start, end;

    /*
     * Get input
     */
    if (argc<4) {
        printf("Usage: a.out number_of_data number_of_threads low_limit\n");
        exit(1);
    }
    N = atoi(argv[1]);
    pthr = atoi(argv[2]);
    low_limit = atoi(argv[3]);
    printf("Number of Data: %d\n", N);
    printf("Number of Threads: %d\n", pthr);
    printf("Low Limit : %d\n", low_limit);
    printf("\n");

    /*
     * Generate the array
     */
    Data = (float *) malloc (sizeof(float)*N);
    Databak1 = (float *) malloc (sizeof(float)*N);
    Databak2 = (float *) malloc (sizeof(float)*N);
    if (Data == NULL || Databak1 == NULL || Databak2 == NULL) {
        printf("Error\n");
        exit(1);
    }
    for (i=0; i<N; i++) {
        Databak1[i] = Databak2[i] = Data[i] = 1.1 * rand() * 5000 / RAND_MAX;
    }
        
    /* 
     * Check the results using the qsort routine in stdlib 
     */
    start = gethrtime();
    qsort ((void *)&Databak1[0], N, sizeof(float), floatcompare);
    end = gethrtime();
    printf("Sequential qsort() Time: %lld ms\n", (end-start)/1000000);

    /* 
     * Check the results using the seq quick sort
     */
    start = gethrtime();
    seq_quick_sort (0, N-1, &Databak2[0]);
    end = gethrtime();
    printf("Sequential quick_sort() Time: %lld ms\n", (end-start)/1000000);

    /*
     * Quick sort using OMP Task
     */
    omp_set_num_threads(pthr);
    omp_set_dynamic(0);
    start = gethrtime();
    #pragma omp parallel
    {
        #pragma omp single nowait
        par_quick_sort (0, N-1, &Data[0], low_limit);
    }
    end = gethrtime();
    printf("Parallel quick_sort() Time: %lld ms\n", (end-start)/1000000);

    int error;
    error = 0;
    for (i=0; i<N; i++) {
        if (Databak1[i] != Data[i] || Databak2[i] != Data[i]) {
            printf("%d: %f %f %f\n", i, Databak1[i], Databak2[i], Data[i]);
            error = 1;
            break;
        }
    }
    printf("\n");
    if (error)
        printf("Wrong !\n");
    else
        printf("Correct\n");
          
    return 0;
}
