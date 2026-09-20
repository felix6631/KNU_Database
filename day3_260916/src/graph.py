from matplotlib import pyplot as plt 
import pandas as pd 
import pymysql 

conn = pymysql.connect(
        host="localhost",
        port=3306,
        user="root",
        password="",
        database="week3",
        charset="utf8mb4"
)
gender = ['male','female']
first = ['E','I']
third = ['T','F']

results = []

with conn.cursor() as cursor:
    cursor.execute("SELECT DATABASE()")
    print(cursor.fetchone())
    cursor.execute("SELECT DISTINCT gender FROM mbti")
    print(cursor.fetchall())
    cursor.execute("SELECT DISTINCT mbti_type FROM mbti")
    print(cursor.fetchall())


try:
    with conn.cursor() as cursor:
        for g in gender:
            for f in first:
                for t in third:
                    query = f"SELECT count(*) FROM mbti WHERE mbti_type LIKE '{f}_{t}_' AND gender='{g}'"
                    cursor.execute(query)
                    count = cursor.fetchone()[0]
                    print(cursor.fetchall())
                    results.append({'gender':g, 'type':f+'_'+t+'_', 'count':count})
finally:
    conn.close()


df = pd.DataFrame(results)
print(df)

pivot = df.pivot(index='type', columns='gender', values='count')

pivot.plot(kind='bar', figsize=(8, 5))
plt.title('MBTI COMP0322_003')
plt.xlabel('type')
plt.ylabel('cont')
plt.xticks(rotation=0)
plt.legend(title='gender')
plt.tight_layout()
plt.savefig('graph.png', dpi=150)
