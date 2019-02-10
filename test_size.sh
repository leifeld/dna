
if [[ $1 == *"manual"* ]]; then
  minimumsize=1
  else
  minimumsize=8100000
fi
actualsize=$(wc -c <$1)
if [ $actualsize -ge $minimumsize ]; then
    echo size is $actualsize
    exit 0
else
    echo size is too small: $actualsize
    exit 1
fi 
