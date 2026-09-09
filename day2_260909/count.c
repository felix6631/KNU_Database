#include <stdio.h>
#include <string.h>
#include <stdlib.h>



int main() {
    int *hashes = (int*)calloc(2000000, sizeof(int));
    FILE* fp = fopen("customer2000000.txt", "r");
    if (fp == NULL) {
        printf("Failed to open the file.\n");
        return 1;
    }

    char line[1000];
    char *name, *prod, *date;
    while (fgets(line, sizeof(line), fp)) {
        name = strtok(line,",");
        int num = atoi(name + 6);
        int hash = num;
        if (hashes[hash] == 1) {
            printf("Duplicate found: %s\n", line);
        } else {
            hashes[hash] = 1;
        }
    }

    fclose(fp);
    free(hashes);
    return 0;
}