from sklearn.externals import joblib
from java import cast, jarray, jboolean, jbyte, jchar, jclass, jint, jdouble


def test(path):
    # x_t = jarray(jdouble)([22.0, 1.0, 0.0, 172.7, 76.7, 2.0, 2.0, 2.0, 2.0, 0.0, 0.0, 2.0, 4.0, 0.0, 0.989, 0.0])
    y_t = 3
    xx = jarray(jdouble)([5.1,3.5,1.4,0.2])
    # 模型加载

    clf = joblib.load("storage/data/train_model.m")

    # 模型预测
    # y_prob = clf.predict(x_t, num_iteration=clf.best_iteration)
    # y_pred = [list(x).index(max(x)) for x in y_prob]
    # print("预测结果：", y_pred)

    # score = metrics.accuracy_score(y_pred=y_pred, y_true=y_t)
    # precision = metrics.precision_score(y_true=y_t, y_pred=y_pred, average=None)
    # recall = metrics.recall_score(y_true=y_t, y_pred=y_pred, average=None)
    # matrix = metrics.confusion_matrix(y_true=y_t, y_pred=y_pred)
    # FAR = [(matrix[1, 0] + matrix[2, 0] + matrix[3, 0]) / (
    #         matrix[1, 0] + matrix[2, 0] + matrix[3, 0] + matrix[1, 1] + matrix[2, 2] + matrix[3, 3]),
    #        (matrix[0, 1] + matrix[2, 1] + matrix[3, 1]) / (
    #                matrix[0, 1] + matrix[2, 1] + matrix[3, 1] + matrix[0, 0] + matrix[2, 2] + matrix[3, 3]),
    #        (matrix[1, 2] + matrix[0, 2] + matrix[3, 2]) / (
    #                matrix[1, 2] + matrix[0, 2] + matrix[3, 2] + matrix[1, 1] + matrix[0, 0] + matrix[3, 3]),
    #        (matrix[1, 3] + matrix[0, 3] + matrix[2, 3]) / (
    #                matrix[1, 3] + matrix[0, 3] + matrix[2, 3] + matrix[1, 1] + matrix[0, 0] + matrix[2, 2]),
    #        ]  # FP/(FP+TN)
    # print(matrix)
    # print("AUC score: {:<8.5f}".format(score))
    # print('precision' + str(precision))
    # print('recall' + str(recall))
    # print(" FAR" + str(FAR))