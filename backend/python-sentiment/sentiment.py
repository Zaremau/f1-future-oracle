import sys
from textblob import TextBlob

# Текст новости передается как аргумент командной строки
if __name__ == "__main__":
    if len(sys.argv) > 1:
        text = sys.argv[1]
        blob = TextBlob(text)
        # Выводим полярность (от -1.0 до 1.0)
        print(blob.sentiment.polarity)
    else:
        print(0.0)