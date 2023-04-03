#include <stdlib.h>
#include "qsort.h"

void quick_sort (int p, int r, float *data, int low_limit)
{
    if (p < r) {
        if ((r-p) < low_limit) 
            qsort ((void *)&data[p], r-p+1, sizeof(float), floatcompare);
        else {
            int q = partition (p, r, data);
            #pragma omp parallel sections firstprivate(data, p, q, r)
            {
                #pragma omp section
                quick_sort (p, q-1, data, low_limit);
                #pragma omp section
                quick_sort (q+1, r, data, low_limit);
            }
        }
    }
}

void par_quick_sort (int n, float *data, int low_limit)
{
    quick_sort (0, n, data, low_limit);
}
