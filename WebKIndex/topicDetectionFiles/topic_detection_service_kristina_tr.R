arguments = commandArgs(trailingOnly = TRUE)


input_folder = arguments[1]
#input_folder = "1472653593542"


print(input_folder)




library(tm)
library(RWeka)
corp = VCorpus(DirSource(input_folder, encoding = "UTF-8", mode = "text"))

input_stopwords_TR = "turkish_stopwords.txt"

stopwords_TR_list = readLines(input_stopwords_TR)

corp = tm_map(corp, removeWords, stopwords_TR_list)
corp = tm_map(corp, removePunctuation)
corp = tm_map(corp, removeNumbers)

dtm1 = DocumentTermMatrix(corp, control=list(bounds = list(global = c(3,Inf))))
dtm1_forLDA = DocumentTermMatrix(corp, control=list(bounds = list(global = c(3,Inf))))
rowTotals = apply(dtm1_forLDA , 1, sum)
dtm1_forLDA = dtm1_forLDA[rowTotals> 0, ]
dtm1_forDBSCAN = as.matrix(dtm1)

# l1 normalization
for(j in 1:dim(dtm1_forDBSCAN)[2]) dtm1_forDBSCAN[,j] = dtm1_forDBSCAN[,j]/sum(dtm1_forDBSCAN[,j])



#optics_results = optics(dtm1_forDBSCAN, eps = 1, minPts = 4)
#plot(optics_results$reachdist[optics_results$order], type = "h", col = "blue", ylab = "Reachability distance", xlab = "OPTICS order")




### DBSCAN-Martingale
library(dbscan)
minpts = 4
T = 5

### generate 5 random numbers from the uniform distribution in [0, 0.1]
random.epsilon = runif(T, min=0, max = 0.2)
random.epsilon = sort(random.epsilon, decreasing = FALSE)
dbscan.results.all = matrix(0, nrow = dim(dtm1)[1], ncol=T)

for(j in 1:T) dbscan.results.all[,j] = dbscan(dtm1_forDBSCAN, random.epsilon[j], minpts)$cluster
# giant cluster removal
for(j in 1:T) {
  for(i in 1:length(dbscan.results.all[,j])) dbscan.results.all[i,j]= dbscan.results.all[i,j] - 1
  for(i in 1:length(dbscan.results.all[,j])) if(dbscan.results.all[i,j]==-1) dbscan.results.all[i,j]=0
}
principal.clustering = dbscan.results.all[,1]
for(j in 1:T) {
  if((principal.clustering%*%dbscan.results.all[,j])[1,1]==0) {
    b = max(principal.clustering)
    for(i in 1:length(dbscan.results.all[,j])) if(dbscan.results.all[i,j]!=0) dbscan.results.all[i,j] = dbscan.results.all[i,j] + b
    principal.clustering = principal.clustering + dbscan.results.all[,j]
  } else {
    h = c()
    clh = c()
    for(i in 1:length(principal.clustering)) {
      h = c(h,0)
      clh = c(clh,0)
    }
    for(i in 1:length(principal.clustering)) if(principal.clustering[i]==0 && dbscan.results.all[i,j] != 0) h[i]=dbscan.results.all[i,j]
    b = max(principal.clustering)
    u = 0
    if(max(h)>0) {
      for(j in 1:max(h)) if(sum(h==j)>=minpts) {
        u = u + 1
        clh[which(h==j)]= u
      }
      for(i in 1:length(principal.clustering)) if(clh[i]!=0) clh[i] = clh[i] + b
      principal.clustering = principal.clustering + clh
    }
  }
}
num_of_topics = max(principal.clustering)
#num_of_topics = num_of_topics*2

### LDA using the num_of_topics
library(topicmodels)
k = if(num_of_topics<2) 2 else num_of_topics
k = if(num_of_topics>5) 5 else num_of_topics

LDA.results = LDA(dtm1_forLDA, k)
LDA.clustering.vector = rep(0, dtm1_forLDA$nrow)
for(i in 1:dtm1_forLDA$nrow) LDA.clustering.vector[i] = which.max(LDA.results@gamma[i,])

### assign the documents in each topic
topics.list.IDs = vector("list", k+1)
names(topics.list.IDs) = as.character(0:k)

### get probabilities for the documents for each topic
doc_probs = posterior(LDA.results)$topics

for(i in 1:k) {
  doc_probs_sorted <- doc_probs[order(doc_probs[,i], decreasing = TRUE),]
  topics.list.IDs[[i+1]] = vector("list", 4)
  names(topics.list.IDs[[i+1]]) = c("labels", "scores", "articles", "top_ranked_docs")
  topics.list.IDs[[i+1]][[1]] = paste(LDA.results@terms[sort(LDA.results@beta[i,], decreasing = TRUE, index.return = TRUE)$ix[1:8]], collapse = " ")
  num_labels = length(strsplit(topics.list.IDs[[i+1]][[1]], split = " ")[[1]])
  topics.list.IDs[[i+1]][[2]] = 1 - abs(sort(LDA.results@beta[i,], decreasing = TRUE)[1:num_labels])/abs(sort(LDA.results@beta[i,], decreasing = TRUE)[num_labels])
  topics.list.IDs[[i+1]][[3]] = dtm1_forLDA$dimnames$Docs[which(LDA.clustering.vector==i)]
  doc_probs_number = 0
  if(length(which(LDA.clustering.vector==i))<10) doc_probs_number = length(which(LDA.clustering.vector==i)) else doc_probs_number = 10
  topics.list.IDs[[i+1]][[4]] = rownames(doc_probs_sorted)[1:doc_probs_number]
}
# create a collection of "noise"-empty documents
if(length(which(rowTotals==0))>0) {
  topics.list.IDs[[1]] = vector("list", 2)
  names(topics.list.IDs[[1]]) = c("labels", "articles")
  topics.list.IDs[[1]][[1]] = "noise"
  topics.list.IDs[[1]][[2]] = dtm1$dimnames$Docs[which(rowTotals==0)]
} else topics.list.IDs = topics.list.IDs[-1]

### write the results to a JSON file
library(rjson)
exportJSON = toJSON(topics.list.IDs)
splitted=strsplit(input_folder, "/")[[1]]
write(exportJSON, file = paste("topics_",splitted[2],".json", sep = ""))

#write(exportJSON, file = "topics_ilias.json")
