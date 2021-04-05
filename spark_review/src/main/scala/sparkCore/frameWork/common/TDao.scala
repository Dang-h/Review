package sparkCore.frameWork.common

import sparkCore.frameWork.util.EnvUtil

trait TDao {
	def readFile(path: String) = {
		EnvUtil.take().textFile(path)
	}
}
