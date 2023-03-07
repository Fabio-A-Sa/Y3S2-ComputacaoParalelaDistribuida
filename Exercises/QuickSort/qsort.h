extern int floatcompare(const void *p1, const void *p2);
extern int partition (int p, int r, float *data);
extern void quick_sort (int p, int r, float *data, int low_limit);
extern void par_quick_sort (int n, float *data, int low_limit);
