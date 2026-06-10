import { PageContainer, ProCard, ProDescriptions } from '@ant-design/pro-components';
import { Button, Card, Col, Empty, message, Row, Tag } from 'antd';
import React, { useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import { getVenueDetail, getVenueCourts } from '@/services/app';
import { EnvironmentOutlined, PhoneOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { getVenueImage } from '@/utils/venueImages';

const VenueDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [venue, setVenue] = useState<API.Venue | null>(null);
  const [courts, setCourts] = useState<API.Court[]>([]);
  const [loading, setLoading] = useState(false);

  // 加载球馆详情
  const loadVenue = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await getVenueDetail(Number(id));
      setVenue(response.data);
    } catch (error: any) {
      message.error(error.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载场地列表
  const loadCourts = async () => {
    if (!id) return;
    try {
      const response = await getVenueCourts(Number(id));
      setCourts(response.data);
    } catch (error: any) {
      message.error(error.message || '加载场地失败');
    }
  };

  useEffect(() => {
    loadVenue();
    loadCourts();
  }, [id]);

  // 进入场地预约页
  const handleCourtClick = (courtId: number) => {
    history.push(`/app/courts/${courtId}`);
  };

  if (!venue) {
    return <PageContainer loading={loading} />;
  }

  return (
    <PageContainer
      title={venue.name}
      subTitle="球馆详情与场地信息"
      onBack={() => history.back()}
    >
      <ProCard style={{ marginBottom: 16, overflow: 'hidden' }}>
        <img
          src={getVenueImage(venue.id)}
          alt={venue.name}
          style={{ width: '100%', height: 260, objectFit: 'cover', borderRadius: 12 }}
        />
      </ProCard>

      <ProCard title="球馆信息" style={{ marginBottom: 16 }}>
        <ProDescriptions column={2}>
          <ProDescriptions.Item label="球馆名称">{venue.name}</ProDescriptions.Item>
          <ProDescriptions.Item label="联系电话">
            <PhoneOutlined /> {venue.phone || '暂无'}
          </ProDescriptions.Item>
          <ProDescriptions.Item label="地址" span={2}>
            <EnvironmentOutlined /> {venue.address}
          </ProDescriptions.Item>
          <ProDescriptions.Item label="营业时间">
            <ClockCircleOutlined /> {venue.openTime} - {venue.closeTime}
          </ProDescriptions.Item>
          <ProDescriptions.Item label="状态">
            <Tag color={venue.status === 'enabled' ? 'green' : 'red'}>
              {venue.status === 'enabled' ? '营业中' : '已停业'}
            </Tag>
          </ProDescriptions.Item>
        </ProDescriptions>
      </ProCard>

      <ProCard title="场地列表">
        {courts.length === 0 ? (
          <Empty description="暂无场地" />
        ) : (
          <Row gutter={[16, 16]}>
            {courts.map((court) => (
              <Col xs={24} sm={12} md={8} lg={6} key={court.id}>
              <Card
                hoverable
                data-testid={`court-card-${court.id}`}
                style={{ borderRadius: 14 }}
                onClick={() => handleCourtClick(court.id)}
                actions={[
                  <Button type="primary" key="book" data-testid={`book-court-${court.id}`}>
                      立即预约
                    </Button>,
                  ]}
                >
                  <Card.Meta
                    title={court.name}
                    description={
                      <div>
                        <div>类型：{court.type}</div>
                        <div style={{ color: '#ff4d4f', fontSize: 16, fontWeight: 'bold', marginTop: 8 }}>
                          ¥{court.pricePerSlot}/小时
                        </div>
                        <div style={{ marginTop: 4 }}>
                          <Tag color={court.status === 'enabled' ? 'green' : 'red'}>
                            {court.status === 'enabled' ? '可预约' : '已禁用'}
                          </Tag>
                        </div>
                      </div>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </ProCard>
    </PageContainer>
  );
};

export default VenueDetail;
