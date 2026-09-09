lst = []
cnt = 0
with open("customer2000000.txt", "r") as f:
    for line in f:
        if line.strip() in lst:
            print("Duplicate found:", line.strip())
        else:
            lst.append(line.strip())
        cnt += 1

        if cnt % 100000 == 0:
            print(f"Processed {cnt} lines.")
    